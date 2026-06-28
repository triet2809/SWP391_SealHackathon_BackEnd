# TEAM_SPECIFICATION.md — BE3 Team Management API

> **Jira Task:** BE3 — Create Team & Member Management API
> **Status:** Phase 1 — Specification
> **Dependencies:** Event API (BE1), Auth API (BE2)

---

## 1. Scope

This specification covers the complete behaviour of the following APIs:

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/events/{eventId}/teams` | List all teams for an event |
| `POST` | `/events/{eventId}/teams` | Create a new team |
| `GET` | `/teams/{teamId}` | Get team detail |
| `PUT` | `/teams/{teamId}` | Update team info (name, description) |
| `DELETE` | `/teams/{teamId}` | Delete a team |
| `GET` | `/teams/{teamId}/members` | List team members |
| `POST` | `/teams/{teamId}/members/invite` | Invite a user to the team |
| `DELETE` | `/team-members/{teamMemberId}` | Remove a member from the team |
| `PATCH` | `/team-members/{teamMemberId}/role` | Change a member's role |
| `PATCH` | `/team-members/{teamMemberId}/accept` | Accept an invitation |
| `PATCH` | `/team-members/{teamMemberId}/decline` | Decline an invitation |

---

## 2. Acceptance Criteria

| # | Criterion | Source |
|---|---|---|
| AC-01 | Event registration must be `registration_open` for all write operations | Business requirement |
| AC-02 | Team name must be unique within an event (case-insensitive) | Schema constraint `uq_teams_event_lower_name` |
| AC-03 | Team size (invited + accepted) must never exceed `event.max_team_size` | Schema trigger `enforce_team_member_rules` |
| AC-04 | Team creator is automatically assigned the `LEADER` role as the first accepted member | Business requirement |
| AC-05 | A user may only belong to one team (invited or accepted) per event at a time | Schema trigger `enforce_team_member_rules` |
| AC-06 | Only the team leader may invite members, remove members, and change roles | Permission rule |
| AC-07 | Only the invited user may accept or decline their own invitation | Permission rule |
| AC-08 | Locked teams (`locked_at IS NOT NULL`) do not allow any member modifications | Business requirement |
| AC-09 | Eliminated and disqualified teams are immutable | Schema trigger; terminal states |
| AC-10 | All state transitions are recorded in `audit_logs` | Audit requirement |

---

## 3. Validation Rules

### 3.1 Common Guards (applied before any write operation)

| Rule | Check | Error |
|---|---|---|
| VR-01 | Authenticated user's `account_status = 'approved'` | 403 |
| VR-02 | Target event exists | 404 |
| VR-03 | Target team exists | 404 |
| VR-04 | Target team member row exists | 404 |

### 3.2 Create Team

| Rule | Check | Error |
|---|---|---|
| VR-10 | `event.status = 'registration_open'` | 409 |
| VR-11 | `LOWER(request.name)` not already used by another team in this event | 409 |
| VR-12 | Requesting user is not already `invited` or `accepted` in any team in this event | 409 |
| VR-13 | `request.name` is non-blank, max 255 characters | 400 |

### 3.3 Invite Member

| Rule | Check | Error |
|---|---|---|
| VR-20 | Caller holds `role = 'LEADER'` with `status = 'accepted'` in this team | 403 |
| VR-21 | `event.status = 'registration_open'` | 409 |
| VR-22 | Team `status NOT IN ('eliminated', 'disqualified')` | 409 |
| VR-23 | `team.locked_at IS NULL` | 409 |
| VR-24 | Count of team_members with `status IN ('invited', 'accepted')` < `event.max_team_size` | 409 |
| VR-25 | Target user `account_status = 'approved'` | 422 |
| VR-26 | Target user not already `invited` or `accepted` in any team in this event | 409 |
| VR-27 | Target user is not the caller themselves | 400 |

### 3.4 Accept Invitation

| Rule | Check | Error |
|---|---|---|
| VR-30 | Caller is the user referenced in the `team_members` row | 403 |
| VR-31 | `team_member.status = 'invited'` | 409 |
| VR-32 | `event.status = 'registration_open'` | 409 |
| VR-33 | Team `status NOT IN ('eliminated', 'disqualified')` | 409 |
| VR-34 | `team.locked_at IS NULL` | 409 |
| VR-35 | Count of `accepted` members < `event.max_team_size` (capacity check on accepted only, since accepting converts invited→accepted) | 409 |

### 3.5 Decline Invitation

| Rule | Check | Error |
|---|---|---|
| VR-40 | Caller is the user referenced in the `team_members` row | 403 |
| VR-41 | `team_member.status = 'invited'` | 409 |

> No registration-open check on decline — freeing a slot is always permitted.

### 3.6 Delete Team Member (Remove)

| Rule | Check | Error |
|---|---|---|
| VR-50 | Caller holds `role = 'LEADER'` with `status = 'accepted'` in this team | 403 |
| VR-51 | Target member is not the caller themselves (leader cannot self-remove) | 409 |
| VR-52 | Target `team_member.status IN ('invited', 'accepted')` | 409 |
| VR-53 | `event.status = 'registration_open'` | 409 |
| VR-54 | Team `status NOT IN ('eliminated', 'disqualified')` | 409 |
| VR-55 | `team.locked_at IS NULL` | 409 |

### 3.7 Change Member Role

| Rule | Check | Error |
|---|---|---|
| VR-60 | Caller holds `role = 'LEADER'` with `status = 'accepted'` in this team | 403 |
| VR-61 | Target member is not the caller themselves | 409 |
| VR-62 | Target `team_member.status = 'accepted'` | 409 |
| VR-63 | `event.status = 'registration_open'` | 409 |
| VR-64 | Team `status NOT IN ('eliminated', 'disqualified')` | 409 |
| VR-65 | `team.locked_at IS NULL` | 409 |
| VR-66 | `newRole` is a valid `TeamMemberRole` value (`LEADER` or `MEMBER`) | 400 |
| VR-67 | If `newRole = LEADER`, demote current leader to `MEMBER` atomically | — |

### 3.8 Update Team (PUT)

| Rule | Check | Error |
|---|---|---|
| VR-70 | Caller holds `role = 'LEADER'` with `status = 'accepted'` in this team | 403 |
| VR-71 | `event.status = 'registration_open'` | 409 |
| VR-72 | Team `status NOT IN ('eliminated', 'disqualified')` | 409 |
| VR-73 | `team.locked_at IS NULL` | 409 |
| VR-74 | New `name` (if changed) is unique within event (case-insensitive) | 409 |

### 3.9 Delete Team

| Rule | Check | Error |
|---|---|---|
| VR-80 | Caller holds `role = 'LEADER'` with `status = 'accepted'` in this team | 403 |
| VR-81 | `event.status = 'registration_open'` | 409 |
| VR-82 | Team `status = 'waiting_for_members'` (only pre-approved teams can be self-deleted) | 409 |

> Teams that have reached `approved_open` or higher can only be eliminated by the coordinator, not deleted by the leader. This prevents leaders from gaming the system by deleting a team and re-registering.

---

## 4. Permissions Matrix

| Operation | PARTICIPANT (no team) | TEAM MEMBER | TEAM LEADER | COORDINATOR |
|---|---|---|---|---|
| List teams | ✅ | ✅ | ✅ | ✅ |
| Get team detail | ✅ | ✅ | ✅ | ✅ |
| Create team | ✅ | ❌ | ❌ | ✅ |
| Update team | ❌ | ❌ | ✅ (own team) | ✅ |
| Delete team | ❌ | ❌ | ✅ (own, waiting only) | ✅ |
| List members | ✅ | ✅ | ✅ | ✅ |
| Invite member | ❌ | ❌ | ✅ (own team) | ✅ |
| Remove member | ❌ | ❌ | ✅ (own team) | ✅ |
| Change role | ❌ | ❌ | ✅ (own team) | ✅ |
| Accept invite | ❌ | ❌ (already in a team) | ❌ | — |
| Decline invite | The invited user only | — | — | — |

---

## 5. Edge Cases

### 5.1 Team Size Edge Cases

| Case | Behaviour |
|---|---|
| Leader accepts → team hits `max_team_size` | `sync_team_status` auto-sets `approved_full`, sets `locked_at` |
| Accepted member leaves → team drops below `min_team_size` | `sync_team_status` auto-sets `waiting_for_members`, clears `approved_at` |
| Invited member declines → team was `approved_full` | Slot freed, `sync_team_status` → `approved_open`, `locked_at` cleared |
| Team at `max_team_size` with some `invited` (not yet accepted) | Capacity full for new invites, but team is not `approved_full` yet |

### 5.2 Leadership Edge Cases

| Case | Behaviour |
|---|---|
| Leader tries to invite themselves | Blocked (VR-27): cannot invite self |
| Leader tries to remove themselves | Blocked (VR-51): cannot self-remove |
| Role change to `LEADER` when target is already `LEADER` | No-op or 409; already has the role |
| Role change to `MEMBER` when target is already `MEMBER` | No-op or 409; already has the role |
| Transfer leadership: new leader promotion | Current leader → `MEMBER`, target → `LEADER` in one atomic transaction |

### 5.3 Registration Window Edge Cases

| Case | Behaviour |
|---|---|
| Event moves from `registration_open` to `registration_closed` while a user has a pending invite | The invite becomes unacceptable; user gets 409 on accept attempt |
| Team is at `waiting_for_members` when registration closes | Team is automatically eliminated by `close_event_registration()` |
| User tries to create team in wrong event status (`draft`, `ongoing`, etc.) | 409 CONFLICT — registration not open |

### 5.4 Duplicate and Concurrency Edge Cases

| Case | Behaviour |
|---|---|
| Two users simultaneously create teams with the same name | DB unique constraint fires; one gets 409 |
| Two users simultaneously accept invite to the same last slot | DB trigger fires; one gets 409 (capacity exceeded) |
| Coordinator locks team while invite is in-flight | Invite can still exist but accept will be blocked by VR-34 |

---

## 6. Error Scenarios Reference

| Code | HTTP Status | When |
|---|---|---|
| `TEAM_NOT_FOUND` | 404 | `teamId` does not exist |
| `EVENT_NOT_FOUND` | 404 | `eventId` does not exist |
| `TEAM_MEMBER_NOT_FOUND` | 404 | `teamMemberId` does not exist |
| `USER_NOT_FOUND` | 404 | Invited `userId`/`email` does not exist |
| `ACCOUNT_NOT_APPROVED` | 403 | Caller's account is `pending` or `rejected` |
| `FORBIDDEN_NOT_LEADER` | 403 | Caller is not the team leader |
| `FORBIDDEN_NOT_MEMBER_OWNER` | 403 | Caller is not the owner of the invite |
| `REGISTRATION_NOT_OPEN` | 409 | Event is not in `registration_open` status |
| `TEAM_NAME_DUPLICATE` | 409 | Team name already taken in event (case-insensitive) |
| `USER_ALREADY_IN_TEAM` | 409 | Target user already invited/accepted in this event |
| `TEAM_FULL` | 409 | Team at `max_team_size` capacity |
| `TEAM_LOCKED` | 409 | `team.locked_at IS NOT NULL` |
| `TEAM_TERMINATED` | 409 | Team is `eliminated` or `disqualified` |
| `MEMBER_STATUS_INVALID` | 409 | Operation not valid for current member status |
| `CANNOT_SELF_REMOVE` | 409 | Leader attempting to remove themselves |
| `CANNOT_SELF_INVITE` | 400 | Leader attempting to invite themselves |
| `CANNOT_CHANGE_OWN_ROLE` | 409 | Leader attempting to change own role |
| `TEAM_NOT_DELETABLE` | 409 | Team has already reached approved status |
| `TARGET_USER_NOT_APPROVED` | 422 | Invited user's account is not approved |
| `INVALID_ROLE` | 400 | Role value is not `LEADER` or `MEMBER` |

---

## 7. Database Side Effects

Every write operation must account for automatic DB behaviour:

| Trigger | Table | When Fires | Effect |
|---|---|---|---|
| `trg_team_members_enforce_rules` | `team_members` | BEFORE INSERT/UPDATE | Validates max size, event conflict, terminal team status |
| `trg_team_members_after_changed` | `team_members` | AFTER INSERT/UPDATE/DELETE | Calls `sync_team_status()` → auto-updates `teams.status`, `locked_at`, `approved_at` |
| `trg_team_join_requests_enforce_rules` | `team_join_requests` | BEFORE INSERT/UPDATE | Validates team capacity, user conflict, team status |
| `uq_team_members_one_active_leader` | `team_members` | On INSERT/UPDATE | Rejects second active leader |
| `uq_teams_event_lower_name` | `teams` | On INSERT/UPDATE | Rejects duplicate team name |

> The service layer must **not** set `teams.status` manually during member operations. It must read the refreshed status after the transaction commits.

---

## 8. Audit Log Requirements

Every mutating API must write an `audit_logs` entry within the same transaction:

| API | `audit_action` enum value |
|---|---|
| Create team | `CREATE_TEAM` |
| Invite member | `REQUEST_JOIN_TEAM` *(approximated for invite flow)* |
| Accept invitation | `APPROVE_JOIN_REQUEST` *(approximated)* |
| Decline invitation | `REJECT_JOIN_REQUEST` *(approximated)* |
| Remove member | `UPDATE` with `target_type = 'team_member'` |
| Change role | `UPDATE` with `target_type = 'team_member'` |
| Update team | `UPDATE` with `target_type = 'team'` |
| Delete team | `DELETE` with `target_type = 'team'` |