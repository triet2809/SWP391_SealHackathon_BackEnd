# TEAM_TEST_CASES.md — BE3 Team Management Test Cases

> **Jira Task:** BE3 — Create Team & Member Management API
> **Test Type:** Specification-level test cases (to be implemented as JUnit + Mockito unit tests and Spring Boot integration tests)

---

## Legend

| Symbol | Meaning |
|---|---|
| ✅ | Expected success |
| ❌ | Expected failure |
| `→` | Leads to |
| `PRE` | Precondition |
| `POST` | Postcondition (what to assert after the call) |

---

## Group 1 — Create Team

---

### TC001 — Create Team Success

**Type:** Integration
**API:** `POST /events/{eventId}/teams`

**PRE:**
- User `userA` exists, `account_status = 'approved'`
- Event `event1` exists, `status = 'registration_open'`, `min_team_size = 3`, `max_team_size = 5`
- No team named `"Alpha Squad"` exists in `event1`
- `userA` is not already in any team in `event1`

**Request:**
```json
{
  "name": "Alpha Squad",
  "description": "We build fast."
}
```

**Expected:** ✅ `201 CREATED`

**POST:**
- Response body contains `id`, `status = "WAITING_FOR_MEMBERS"`, `registrationMode = "NEW"`
- `members` array contains exactly 1 entry: `userA` with `role = "LEADER"`, `status = "ACCEPTED"`, `acceptedAt != null`
- `teams` table has 1 new row with the correct `event_id`, `name`, `created_by = userA.id`
- `team_profiles` table has 1 new row linked to the team
- `team_members` table has 1 new row: `role = 'leader'`, `status = 'accepted'`, `accepted_at != null`
- `audit_logs` table has 1 new row: `action = 'CREATE_TEAM'`, `user_id = userA.id`

---

### TC002 — Duplicate Team Name (Case Insensitive)

**Type:** Integration
**API:** `POST /events/{eventId}/teams`

**PRE:**
- Team `"Alpha Squad"` already exists in `event1`
- `userB` is a different approved user, not in any team in `event1`

**Request:**
```json
{ "name": "alpha squad" }
```

**Expected:** ❌ `409 CONFLICT`

```json
{
  "error": "TEAM_NAME_DUPLICATE",
  "message": "A team with the name 'alpha squad' already exists in this event."
}
```

**POST:**
- No new `teams` row created
- No new `team_members` row created

---

### TC003 — Registration Closed

**Type:** Integration
**API:** `POST /events/{eventId}/teams`

**PRE:**
- Event `event2` exists, `status = 'registration_closed'`
- `userC` is approved and not in any team

**Request:**
```json
{ "name": "Late Team" }
```

**Expected:** ❌ `409 CONFLICT`

```json
{
  "error": "REGISTRATION_NOT_OPEN"
}
```

**POST:** No new rows in `teams` or `team_members`.

---

### TC003b — Registration Draft (Not Yet Open)

**Type:** Unit
**API:** `POST /events/{eventId}/teams`

**PRE:** Event `status = 'draft'`

**Expected:** ❌ `409 CONFLICT` — `REGISTRATION_NOT_OPEN`

---

### TC003c — User Already In A Team

**Type:** Integration
**API:** `POST /events/{eventId}/teams`

**PRE:**
- `userD` is already `accepted` in `TeamX` for `event1`

**Request:**
```json
{ "name": "New Team" }
```

**Expected:** ❌ `409 CONFLICT` — `USER_ALREADY_IN_TEAM`

---

### TC003d — Account Not Approved

**Type:** Unit
**API:** `POST /events/{eventId}/teams`

**PRE:** `userE` has `account_status = 'pending'`

**Expected:** ❌ `403 FORBIDDEN` — `ACCOUNT_NOT_APPROVED`

---

### TC003e — Blank Team Name

**Type:** Unit
**API:** `POST /events/{eventId}/teams`

**Request:**
```json
{ "name": "" }
```

**Expected:** ❌ `400 BAD REQUEST` — `VALIDATION_ERROR`

---

## Group 2 — Invite Member

---

### TC004 — Invite Member Success

**Type:** Integration
**API:** `POST /teams/{teamId}/members/invite`

**PRE:**
- `userA` is `LEADER` (`accepted`) of `teamAlpha` in `event1` (`registration_open`)
- `teamAlpha.locked_at = null`, `status = 'waiting_for_members'`
- `userB` is approved and not in any team in `event1`
- Team currently has 1 member (the leader); `max_team_size = 5`

**Request:**
```json
{ "userId": "<userB.id>" }
```

**Expected:** ✅ `201 CREATED`

```json
{
  "role": "MEMBER",
  "status": "INVITED",
  "acceptedAt": null
}
```

**POST:**
- New `team_members` row: `user_id = userB.id`, `role = 'member'`, `status = 'invited'`, `accepted_at = null`
- `teams.status` remains `waiting_for_members` (invited doesn't count toward approved threshold)
- `audit_logs` has new entry

---

### TC004b — Invite By Non-Leader

**Type:** Unit
**API:** `POST /teams/{teamId}/members/invite`

**PRE:** `userB` is a `MEMBER` (not leader) of `teamAlpha`

**Expected:** ❌ `403 FORBIDDEN` — `FORBIDDEN_NOT_LEADER`

---

### TC004c — Invite To Full Team

**Type:** Integration
**API:** `POST /teams/{teamId}/members/invite`

**PRE:**
- `teamBeta` has 5 members with `status IN ('invited', 'accepted')` — at `max_team_size = 5`
- Caller is leader

**Expected:** ❌ `409 CONFLICT` — `TEAM_FULL`

---

### TC004d — Invite User Already In Another Team

**Type:** Integration
**API:** `POST /teams/{teamId}/members/invite`

**PRE:** `userZ` is already `accepted` in `teamOmega` for the same event

**Expected:** ❌ `409 CONFLICT` — `USER_ALREADY_IN_TEAM`

---

### TC004e — Invite User With Pending Account

**Type:** Integration
**API:** `POST /teams/{teamId}/members/invite`

**PRE:** Target user `account_status = 'pending'`

**Expected:** ❌ `422 UNPROCESSABLE` — `TARGET_USER_NOT_APPROVED`

---

### TC004f — Invite To Locked Team

**Type:** Integration
**API:** `POST /teams/{teamId}/members/invite`

**PRE:** `team.locked_at IS NOT NULL` (coordinator-locked or capacity-locked)

**Expected:** ❌ `409 CONFLICT` — `TEAM_LOCKED`

---

### TC004g — Invite Self

**Type:** Unit
**API:** `POST /teams/{teamId}/members/invite`

**PRE:** `userId` in request equals caller's ID

**Expected:** ❌ `400 BAD REQUEST` — `CANNOT_SELF_INVITE`

---

### TC004h — Invite To Disqualified Team

**Type:** Integration
**API:** `POST /teams/{teamId}/members/invite`

**PRE:** `team.status = 'disqualified'`

**Expected:** ❌ `409 CONFLICT` — `TEAM_TERMINATED`

---

## Group 3 — Accept Invitation

---

### TC005 — Accept Invitation Success

**Type:** Integration
**API:** `PATCH /team-members/{teamMemberId}/accept`

**PRE:**
- `teamMember1` row: `user_id = userB.id`, `status = 'invited'`
- `teamAlpha.status = 'waiting_for_members'`, `locked_at = null`
- `event1.status = 'registration_open'`
- Accepted count < `min_team_size` (e.g., 1 accepted, min=3)

**Expected:** ✅ `200 OK`

```json
{
  "status": "ACCEPTED",
  "acceptedAt": "<non-null timestamp>"
}
```

**POST:**
- `team_members` row: `status = 'accepted'`, `accepted_at` is set
- `teams.status` remains `waiting_for_members` (still below `min_team_size`)
- If this acceptance makes accepted count = `min_team_size` → `teams.status = 'approved_open'`, `approved_at` set
- If this acceptance makes accepted count = `max_team_size` → `teams.status = 'approved_full'`, `locked_at` set

---

### TC005b — Accept Causes Team To Reach approved_open

**Type:** Integration
**API:** `PATCH /team-members/{teamMemberId}/accept`

**PRE:**
- Team has 2 accepted members, `min_team_size = 3`
- `userB` is `invited`; accepting makes accepted count = 3

**Expected:** ✅ `200 OK`

**POST:**
- `teams.status = 'approved_open'`
- `teams.approved_at` is set to now

---

### TC005c — Accept Non-Own Invite

**Type:** Unit
**API:** `PATCH /team-members/{teamMemberId}/accept`

**PRE:** Caller is `userA`; the `team_members` row belongs to `userB`

**Expected:** ❌ `403 FORBIDDEN` — `FORBIDDEN_NOT_MEMBER_OWNER`

---

### TC005d — Accept Already Accepted Invite

**Type:** Unit
**API:** `PATCH /team-members/{teamMemberId}/accept`

**PRE:** `team_members.status = 'accepted'` already

**Expected:** ❌ `409 CONFLICT` — `MEMBER_STATUS_INVALID`

---

### TC005e — Accept When Registration Closed

**Type:** Integration
**API:** `PATCH /team-members/{teamMemberId}/accept`

**PRE:** `event.status = 'registration_closed'`; member is still `invited`

**Expected:** ❌ `409 CONFLICT` — `REGISTRATION_NOT_OPEN`

---

### TC005f — Accept When Team Now Full (Race Condition)

**Type:** Integration
**API:** `PATCH /team-members/{teamMemberId}/accept`

**PRE:**
- Team was at 4 accepted when invite was sent
- Before this user accepts, another user fills the last slot
- Team is now at `max_team_size = 5` accepted

**Expected:** ❌ `409 CONFLICT` — `TEAM_FULL`

---

## Group 4 — Decline Invitation

---

### TC006 — Decline Invitation Success

**Type:** Integration
**API:** `PATCH /team-members/{teamMemberId}/decline`

**PRE:**
- `teamMember2` row: `user_id = userC.id`, `status = 'invited'`
- Team has 3 accepted + 1 invited (this one); `max_team_size = 5`

**Expected:** ✅ `200 OK`

```json
{
  "status": "DECLINED",
  "declinedAt": "<non-null timestamp>"
}
```

**POST:**
- `team_members.status = 'declined'`, `declined_at` set
- If team was `approved_full` and this was the accepted-slot-holder: `sync_team_status` fires, team may drop to `approved_open`
- Slot is freed (capacity count decreases)

---

### TC006b — Decline Non-Own Invite

**Type:** Unit
**API:** `PATCH /team-members/{teamMemberId}/decline`

**PRE:** Caller is `userA`; row belongs to `userC`

**Expected:** ❌ `403 FORBIDDEN` — `FORBIDDEN_NOT_MEMBER_OWNER`

---

### TC006c — Decline Already Declined

**Type:** Unit
**API:** `PATCH /team-members/{teamMemberId}/decline`

**PRE:** `team_members.status = 'declined'` already

**Expected:** ❌ `409 CONFLICT` — `MEMBER_STATUS_INVALID`

---

### TC006d — Decline When Registration Closed (Allowed)

**Type:** Integration
**API:** `PATCH /team-members/{teamMemberId}/decline`

**PRE:** `event.status = 'registration_closed'`; member is `invited`

**Expected:** ✅ `200 OK` — decline is always permitted (freeing a slot)

---

## Group 5 — Delete Team Member

---

### TC007 — Delete Team Member Success

**Type:** Integration
**API:** `DELETE /team-members/{teamMemberId}`

**PRE:**
- `userA` is `LEADER` of `teamAlpha`
- `userB` is `accepted` in `teamAlpha` (`teamMember3`)
- `event1.status = 'registration_open'`
- `teamAlpha.locked_at = null`, `status = 'approved_open'`

**Expected:** ✅ `204 NO CONTENT`

**POST:**
- `team_members` row for `userB`: `status = 'removed'`, `removed_at` set
- `sync_team_status` fires; if accepted count drops below `min_team_size` → `teams.status = 'waiting_for_members'`, `approved_at` cleared
- `audit_logs` entry added

---

### TC007b — Remove Non-Active Member (Already Removed)

**Type:** Unit
**API:** `DELETE /team-members/{teamMemberId}`

**PRE:** Target `team_members.status = 'removed'`

**Expected:** ❌ `409 CONFLICT` — `MEMBER_STATUS_INVALID`

---

### TC007c — Leader Self-Remove Blocked

**Type:** Unit
**API:** `DELETE /team-members/{teamMemberId}`

**PRE:** Caller (`userA`) is the `LEADER`; `teamMemberId` points to `userA`'s own row

**Expected:** ❌ `409 CONFLICT` — `CANNOT_SELF_REMOVE`

---

### TC007d — Non-Leader Attempts Remove

**Type:** Unit
**API:** `DELETE /team-members/{teamMemberId}`

**PRE:** Caller is `MEMBER` (not leader)

**Expected:** ❌ `403 FORBIDDEN` — `FORBIDDEN_NOT_LEADER`

---

### TC007e — Remove From Locked Team

**Type:** Integration
**API:** `DELETE /team-members/{teamMemberId}`

**PRE:** `team.locked_at IS NOT NULL`

**Expected:** ❌ `409 CONFLICT` — `TEAM_LOCKED`

---

### TC007f — Remove From Disqualified Team

**Type:** Integration
**API:** `DELETE /team-members/{teamMemberId}`

**PRE:** `team.status = 'disqualified'`

**Expected:** ❌ `409 CONFLICT` — `TEAM_TERMINATED`

---

### TC007g — Remove Causes Team to Drop to waiting_for_members

**Type:** Integration
**API:** `DELETE /team-members/{teamMemberId}`

**PRE:**
- Team has exactly `min_team_size = 3` accepted members (status = `approved_open`)
- Leader removes one accepted member

**Expected:** ✅ `204 NO CONTENT`

**POST:**
- `teams.status = 'waiting_for_members'`
- `teams.approved_at = null` (cleared by trigger)

---

## Group 6 — Change Member Role

---

### TC008 — Change Member Role Success (MEMBER → LEADER Transfer)

**Type:** Integration
**API:** `PATCH /team-members/{teamMemberId}/role`

**PRE:**
- `userA` is `LEADER` of `teamAlpha`
- `userB` is `MEMBER` with `status = 'accepted'` (`teamMember4`)
- `event1.status = 'registration_open'`, `locked_at = null`

**Request:**
```json
{ "role": "LEADER" }
```

**Expected:** ✅ `200 OK`

**POST:**
- `userA`'s `team_members` row: `role = 'member'` (demoted)
- `userB`'s `team_members` row: `role = 'leader'` (promoted)
- `uq_team_members_one_active_leader` constraint still satisfied (only 1 active leader)
- `audit_logs` entry added

---

### TC008b — Change Role to MEMBER

**Type:** Integration
**API:** `PATCH /team-members/{teamMemberId}/role`

**PRE:** `userB` is `LEADER`; request to change target (another accepted member) to `MEMBER`

**Request:**
```json
{ "role": "MEMBER" }
```

**Expected:** ✅ `200 OK`

**POST:** Target `role = 'member'`; no leadership transfer needed

---

### TC008c — Change Role of Invited Member (Not Accepted)

**Type:** Unit
**API:** `PATCH /team-members/{teamMemberId}/role`

**PRE:** Target `status = 'invited'`

**Expected:** ❌ `409 CONFLICT` — `MEMBER_STATUS_INVALID`

---

### TC008d — Leader Changing Own Role Blocked

**Type:** Unit
**API:** `PATCH /team-members/{teamMemberId}/role`

**PRE:** `teamMemberId` points to the caller's own row

**Expected:** ❌ `409 CONFLICT` — `CANNOT_CHANGE_OWN_ROLE`

---

### TC008e — Non-Leader Attempts Role Change

**Type:** Unit
**API:** `PATCH /team-members/{teamMemberId}/role`

**PRE:** Caller is `MEMBER`

**Expected:** ❌ `403 FORBIDDEN` — `FORBIDDEN_NOT_LEADER`

---

### TC008f — Invalid Role Value

**Type:** Unit
**API:** `PATCH /team-members/{teamMemberId}/role`

**Request:**
```json
{ "role": "ADMIN" }
```

**Expected:** ❌ `400 BAD REQUEST` — `INVALID_ROLE`

---

### TC008g — Role Change on Locked Team

**Type:** Integration
**API:** `PATCH /team-members/{teamMemberId}/role`

**PRE:** `team.locked_at IS NOT NULL`

**Expected:** ❌ `409 CONFLICT` — `TEAM_LOCKED`

---

## Group 7 — Additional Integration Scenarios

---

### TC009 — Full Happy Path End-to-End

**Type:** Integration (Scenario)

**Sequence:**
1. `userA` creates team → `status = 'waiting_for_members'` ✅
2. `userA` invites `userB` → 2 total slots (1 accepted, 1 invited)
3. `userA` invites `userC` → 3 total slots
4. `userB` accepts → accepted = 2 (still below min=3)
5. `userC` accepts → accepted = 3 → `status = 'approved_open'`, `approved_at` set ✅
6. `userA` invites `userD` → 4 slots
7. `userA` invites `userE` → 5 slots (at max)
8. `userD` accepts → accepted = 4
9. `userE` accepts → accepted = 5 → `status = 'approved_full'`, `locked_at` set ✅
10. `userA` tries to invite `userF` → `409 TEAM_LOCKED` (now full/locked) ✅

---

### TC010 — Registration Closes With Under-Sized Team

**Type:** Integration (Scenario)

**PRE:**
- `teamOmega` has `min_team_size = 3`, only 2 accepted members
- Coordinator calls `close_event_registration(event1.id)`

**Expected:**
- `event1.status = 'registration_closed'`
- `teamOmega.status = 'eliminated'`
- `teamOmega.eliminated_reason = 'Registration closed: team has fewer than minimum accepted members'`
- `teamOmega.eliminated_at` is set
- `audit_logs` contains `CLOSE_REGISTRATION` entry

---

### TC011 — Concurrent Invite Accept Race Condition

**Type:** Integration (Concurrent)

**PRE:**
- Team has 4 accepted, `max_team_size = 5` (1 slot left)
- `userX` and `userY` both have `status = 'invited'`
- Both call `PATCH /team-members/{id}/accept` at the same time

**Expected:**
- Exactly one succeeds with `200 OK`
- The other gets `409 CONFLICT` — `TEAM_FULL` (DB trigger fires on second transaction)
- `teams.status = 'approved_full'` after successful accept

---

### TC012 — Team Name Uniqueness Across Events

**Type:** Integration

**PRE:**
- `event1` has team `"Alpha Squad"`
- `event2` (different event, also `registration_open`) does NOT have `"Alpha Squad"`

**Request on `event2`:**
```json
{ "name": "Alpha Squad" }
```

**Expected:** ✅ `201 CREATED` — names are unique per event, not globally

---

### TC013 — Leadership Transfer Preserves One Leader Constraint

**Type:** Integration

**PRE:**
- `teamAlpha` has `userA` as `LEADER`, `userB` as `MEMBER (accepted)`
- Request to promote `userB` to `LEADER`

**Expected:**
- Atomic transaction: `userA → MEMBER`, `userB → LEADER`
- `uq_team_members_one_active_leader` never violated during the operation
- Final state: exactly one `LEADER` with active status in the team

---

## Test Data Setup Reference

```
event1:
  id: <uuid>
  status: registration_open
  min_team_size: 3
  max_team_size: 5

userA: approved, FPT student (leader in most tests)
userB: approved, FPT student (member/invitee)
userC: approved, external student
userD: approved, FPT student
userE: approved, FPT student
userF: approved (used as extra / blocked invite target)
userPending: account_status = pending
userRejected: account_status = rejected

teamAlpha: event1, created by userA, status = waiting_for_members
teamBeta:  event1, created by userB, status = approved_full (5 members)
teamOmega: event1, status = disqualified
```