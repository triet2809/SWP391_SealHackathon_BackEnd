# Team Lock & Disqualify API

> **Task:** BE3 — Team Lock & Disqualify
> **Base path:** `/api/v1`
> **Auth:** Bearer JWT (or HTTP Basic in dev). Both actions are **coordinator only**.

Lets an Event Coordinator lock a team (freeze its membership) or disqualify it
(terminal state). Every action writes an `audit_logs` row.

---

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| `PATCH` | `/teams/{teamId}/lock` | Lock a team — no more member changes |
| `PATCH` | `/teams/{teamId}/disqualify` | Disqualify a team (terminal) |

Both return `200 OK` with the full `TeamDetailResponse`.

---

## 1. PATCH `/teams/{teamId}/lock`

Sets `teams.locked_at = now()` and `locked_reason`. While locked, the member
endpoints (invite / accept / remove / change-role) and team update/delete are
blocked with `409 TEAM_LOCKED`.

**Request body** (optional):
```json
{ "reason": "Locked for final review" }
```
If omitted, the reason defaults to `"Locked by coordinator"`.

**Validation**

| Check | Error |
|---|---|
| Team exists | `404 TEAM_NOT_FOUND` |
| Caller has the `coordinator` role | `403 FORBIDDEN_NOT_COORDINATOR` |
| Team is not eliminated / disqualified | `409 TEAM_TERMINATED` |
| Team is not already locked | `409 TEAM_ALREADY_LOCKED` |

**Audit:** `action = LOCK_TEAM`, `target_type = team`, `target_id = teamId`.

---

## 2. PATCH `/teams/{teamId}/disqualify`

Sets `teams.status = 'disqualified'` and `disqualified_reason`. This is a
terminal state — `sync_team_status()` no longer touches the team, and all member
operations stay blocked.

**Request body** (required):
```json
{ "reason": "Rule violation: plagiarised submission" }
```

**Validation**

| Check | Error |
|---|---|
| `reason` non-blank, max 1000 chars | `400 VALIDATION_ERROR` |
| Team exists | `404 TEAM_NOT_FOUND` |
| Caller has the `coordinator` role | `403 FORBIDDEN_NOT_COORDINATOR` |
| Team is not already eliminated / disqualified | `409 TEAM_TERMINATED` |

**Audit:** `action = DISQUALIFY_TEAM`, `target_type = team`, `target_id = teamId`.

---

## Implementation notes

- `teams.status`, `locked_at`, `locked_reason` are managed by DB triggers and
  mapped read-only on the `Team` entity, so both actions use native `UPDATE`
  queries (`TeamRepository.lockTeam` / `disqualifyTeam`).
- Coordinator check: `user_roles` joined to `roles` where `roles.name = 'coordinator'`.
- Audit rows are written through the new `AuditLog` entity / `AuditLogRepository`.
- Edge case: `decline` does not check the lock, so a decline on a coordinator-locked
  team can let `sync_team_status()` clear `locked_at` (the column is shared with the
  capacity lock). Acceptable for MVP.
