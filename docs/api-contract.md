# api-contract.md — Team Management API Contract

> **Jira Task:** BE3 — Create Team & Member Management API
> **Base Path:** `/api/v1`
> **Auth:** Bearer JWT required on all endpoints

---

## Global Conventions

### Request Headers
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
Accept: application/json
```

### Standard Error Response
```json
{
  "timestamp": "2026-06-28T10:00:00Z",
  "status": 409,
  "error": "TEAM_NAME_DUPLICATE",
  "message": "A team with the name 'Alpha Squad' already exists in this event.",
  "path": "/api/v1/events/abc/teams"
}
```

### Common Enums

**`TeamStatus`**
```
WAITING_FOR_MEMBERS | APPROVED_OPEN | APPROVED_FULL | ELIMINATED | DISQUALIFIED
```

**`TeamMemberRole`**
```
LEADER | MEMBER
```

**`TeamMemberStatus`**
```
INVITED | ACCEPTED | DECLINED | REMOVED
```

**`RegistrationMode`**
```
NEW | RESUBMITTED | IMPORTED | MANUAL
```

---

## 1. GET `/events/{eventId}/teams`

### Purpose
List all teams registered for a given event. Open to all authenticated users.

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `eventId` | UUID | Yes | Target event ID |

### Query Parameters
| Param | Type | Default | Description |
|---|---|---|---|
| `status` | `TeamStatus` | — | Filter by team status |
| `search` | String | — | Partial match on team name (case-insensitive) |
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | 20 | Page size (max 100) |

### Response DTO — `200 OK`
```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Alpha Squad",
      "status": "APPROVED_OPEN",
      "registrationMode": "NEW",
      "memberCount": 3,
      "maxMemberCount": 5,
      "minMemberCount": 3,
      "isLocked": false,
      "approvedAt": "2026-06-22T10:00:00Z",
      "createdAt": "2026-06-22T09:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
```

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 404 | `EVENT_NOT_FOUND` | `eventId` does not exist |

---

## 2. POST `/events/{eventId}/teams`

### Purpose
Create a new team for an event. The authenticated user becomes the team leader automatically.

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `eventId` | UUID | Yes | Target event ID |

### Request DTO — `CreateTeamRequest`
```json
{
  "name": "Alpha Squad",
  "description": "We build fast."
}
```

| Field | Type | Required | Constraints |
|---|---|---|---|
| `name` | String | Yes | Non-blank, max 255 chars |
| `description` | String | No | Max 1000 chars |

### Response DTO — `201 CREATED` — `TeamDetailResponse`
```json
{
  "id": "uuid",
  "eventId": "uuid",
  "teamProfileId": "uuid",
  "name": "Alpha Squad",
  "description": "We build fast.",
  "status": "WAITING_FOR_MEMBERS",
  "registrationMode": "NEW",
  "isLocked": false,
  "lockedReason": null,
  "minMemberCount": 3,
  "maxMemberCount": 5,
  "approvedAt": null,
  "createdBy": "uuid",
  "createdAt": "2026-06-28T10:00:00Z",
  "members": [
    {
      "id": "uuid",
      "userId": "uuid",
      "fullName": "Nguyen Van A",
      "email": "a@fpt.edu.vn",
      "role": "LEADER",
      "status": "ACCEPTED",
      "joinedAt": "2026-06-28T10:00:00Z",
      "acceptedAt": "2026-06-28T10:00:00Z"
    }
  ]
}
```

### Validation
- `name`: non-blank, max 255 characters
- `description`: optional, max 1000 characters

### Business Flow
1. Validate caller account is approved
2. Validate event exists and `status = 'registration_open'`
3. Validate caller is not already in a team for this event
4. Validate team name uniqueness within event (case-insensitive)
5. Persist `team_profiles` row
6. Persist `teams` row (`status = WAITING_FOR_MEMBERS`, `registration_mode = NEW`)
7. Persist `team_members` row for caller (`role = LEADER`, `status = ACCEPTED`, `accepted_at = now`)
8. Write `audit_logs` entry (`CREATE_TEAM`)
9. Return `TeamDetailResponse`

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 403 | `ACCOUNT_NOT_APPROVED` | Caller account not approved |
| 404 | `EVENT_NOT_FOUND` | `eventId` does not exist |
| 409 | `REGISTRATION_NOT_OPEN` | Event not in `registration_open` status |
| 409 | `USER_ALREADY_IN_TEAM` | Caller is already invited/accepted in another team |
| 409 | `TEAM_NAME_DUPLICATE` | Team name already taken (case-insensitive) |
| 400 | `VALIDATION_ERROR` | `name` blank or too long |

---

## 3. GET `/teams/{teamId}`

### Purpose
Get full detail of a specific team including its current members.

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `teamId` | UUID | Yes | Target team ID |

### Response DTO — `200 OK` — `TeamDetailResponse`
Same structure as `POST /events/{eventId}/teams` response above.

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 404 | `TEAM_NOT_FOUND` | `teamId` does not exist |

---

## 4. PUT `/teams/{teamId}`

### Purpose
Update team name and/or description. Only the team leader may do this.

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `teamId` | UUID | Yes | Target team ID |

### Request DTO — `UpdateTeamRequest`
```json
{
  "name": "Alpha Squad v2",
  "description": "Updated description."
}
```

| Field | Type | Required | Constraints |
|---|---|---|---|
| `name` | String | Yes | Non-blank, max 255 chars |
| `description` | String | No | Max 1000 chars |

### Response DTO — `200 OK` — `TeamDetailResponse`
Same structure as create response.

### Business Flow
1. Validate team exists
2. Validate caller is `LEADER` of this team
3. Validate event `status = 'registration_open'`
4. Validate team not `eliminated`/`disqualified`
5. Validate team not locked
6. If name changed: validate uniqueness within event (case-insensitive)
7. Update `teams` row
8. Write `audit_logs` entry
9. Return `TeamDetailResponse`

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 403 | `FORBIDDEN_NOT_LEADER` | Caller is not the team leader |
| 404 | `TEAM_NOT_FOUND` | `teamId` does not exist |
| 409 | `REGISTRATION_NOT_OPEN` | Registration is closed |
| 409 | `TEAM_TERMINATED` | Team is eliminated or disqualified |
| 409 | `TEAM_LOCKED` | Team is locked |
| 409 | `TEAM_NAME_DUPLICATE` | New name already taken |
| 400 | `VALIDATION_ERROR` | `name` blank or too long |

---

## 5. DELETE `/teams/{teamId}`

### Purpose
Delete a team. Only permitted when team is still in `waiting_for_members` status (has not yet been approved).

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `teamId` | UUID | Yes | Target team ID |

### Response — `204 NO CONTENT`

### Business Flow
1. Validate team exists
2. Validate caller is `LEADER` of this team
3. Validate event `status = 'registration_open'`
4. Validate team `status = 'waiting_for_members'`
5. Soft-delete or hard-delete `team_members` rows
6. Delete `teams` row (cascade deletes members via FK ON DELETE CASCADE)
7. Write `audit_logs` entry (`DELETE`)
8. Return `204 No Content`

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 403 | `FORBIDDEN_NOT_LEADER` | Caller is not the team leader |
| 404 | `TEAM_NOT_FOUND` | `teamId` does not exist |
| 409 | `REGISTRATION_NOT_OPEN` | Registration is closed |
| 409 | `TEAM_NOT_DELETABLE` | Team status is not `waiting_for_members` |

---

## 6. GET `/teams/{teamId}/members`

### Purpose
List all members of a team, including invited, accepted, declined, and removed.

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `teamId` | UUID | Yes | Target team ID |

### Query Parameters
| Param | Type | Default | Description |
|---|---|---|---|
| `status` | `TeamMemberStatus` | — | Filter by member status |

### Response DTO — `200 OK`
```json
[
  {
    "id": "uuid",
    "teamId": "uuid",
    "userId": "uuid",
    "fullName": "Nguyen Van A",
    "email": "a@fpt.edu.vn",
    "studentType": "FPT",
    "role": "LEADER",
    "status": "ACCEPTED",
    "joinedAt": "2026-06-28T10:00:00Z",
    "acceptedAt": "2026-06-28T10:00:00Z",
    "declinedAt": null,
    "removedAt": null
  }
]
```

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 404 | `TEAM_NOT_FOUND` | `teamId` does not exist |

---

## 7. POST `/teams/{teamId}/members/invite`

### Purpose
Invite a user to join the team. Only the team leader may send invitations.

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `teamId` | UUID | Yes | Target team ID |

### Request DTO — `InviteMemberRequest`
```json
{
  "userId": "uuid"
}
```

| Field | Type | Required | Constraints |
|---|---|---|---|
| `userId` | UUID | Yes | Must be a valid, approved user |

### Response DTO — `201 CREATED` — `TeamMemberResponse`
```json
{
  "id": "uuid",
  "teamId": "uuid",
  "userId": "uuid",
  "fullName": "Tran Thi B",
  "email": "b@fpt.edu.vn",
  "studentType": "FPT",
  "role": "MEMBER",
  "status": "INVITED",
  "joinedAt": "2026-06-28T11:00:00Z",
  "acceptedAt": null,
  "declinedAt": null,
  "removedAt": null
}
```

### Business Flow
1. Validate team exists
2. Validate caller is `LEADER` of this team (`accepted` + `role = LEADER`)
3. Validate event `status = 'registration_open'`
4. Validate team not `eliminated`/`disqualified`
5. Validate `team.locked_at IS NULL`
6. Validate team capacity: `invited + accepted < max_team_size`
7. Validate target user exists and `account_status = 'approved'`
8. Validate target user is not caller (`userId != callerId`)
9. Validate target user not already `invited`/`accepted` in any team this event
10. Persist `team_members` row (`role = MEMBER`, `status = INVITED`)
11. DB trigger `trg_team_members_after_changed` fires `sync_team_status()`
12. Write `audit_logs` entry
13. Return `201 TeamMemberResponse`

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 400 | `CANNOT_SELF_INVITE` | `userId` equals caller's ID |
| 400 | `VALIDATION_ERROR` | `userId` missing or invalid |
| 403 | `FORBIDDEN_NOT_LEADER` | Caller is not the team leader |
| 404 | `TEAM_NOT_FOUND` | `teamId` does not exist |
| 404 | `USER_NOT_FOUND` | `userId` does not exist |
| 409 | `REGISTRATION_NOT_OPEN` | Event not in registration_open status |
| 409 | `TEAM_TERMINATED` | Team is eliminated or disqualified |
| 409 | `TEAM_LOCKED` | `team.locked_at IS NOT NULL` |
| 409 | `TEAM_FULL` | Team already at max capacity |
| 409 | `USER_ALREADY_IN_TEAM` | Target user already in a team this event |
| 422 | `TARGET_USER_NOT_APPROVED` | Target user account not yet approved |

---

## 8. DELETE `/team-members/{teamMemberId}`

### Purpose
Remove an invited or accepted member from the team. Only the team leader may do this. The leader cannot remove themselves.

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `teamMemberId` | UUID | Yes | Target team_members row ID |

### Response — `204 NO CONTENT`

### Business Flow
1. Validate `team_members` row exists
2. Resolve team and event from the row
3. Validate caller is `LEADER` of this team
4. Validate target is not the caller themselves
5. Validate target current `status IN ('invited', 'accepted')`
6. Validate event `status = 'registration_open'`
7. Validate team not `eliminated`/`disqualified`
8. Validate `team.locked_at IS NULL`
9. Update `status = 'removed'`, `removed_at = now()`
10. DB trigger fires `sync_team_status()` — team may drop status
11. Write `audit_logs` entry
12. Return `204 No Content`

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 403 | `FORBIDDEN_NOT_LEADER` | Caller is not the team leader |
| 404 | `TEAM_MEMBER_NOT_FOUND` | `teamMemberId` does not exist |
| 409 | `CANNOT_SELF_REMOVE` | Target is the caller (leader self-remove blocked) |
| 409 | `MEMBER_STATUS_INVALID` | Member is already `declined` or `removed` |
| 409 | `REGISTRATION_NOT_OPEN` | Registration is closed |
| 409 | `TEAM_TERMINATED` | Team is eliminated or disqualified |
| 409 | `TEAM_LOCKED` | Team is locked |

---

## 9. PATCH `/team-members/{teamMemberId}/role`

### Purpose
Change a member's role within the team (`LEADER` ↔ `MEMBER`). If the new role is `LEADER`, the existing leader is atomically demoted to `MEMBER`.

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `teamMemberId` | UUID | Yes | Target team_members row ID |

### Request DTO — `ChangeRoleRequest`
```json
{
  "role": "LEADER"
}
```

| Field | Type | Required | Constraints |
|---|---|---|---|
| `role` | `TeamMemberRole` | Yes | Must be `LEADER` or `MEMBER` |

### Response DTO — `200 OK` — `TeamMemberResponse`
Returns the updated member row.

### Business Flow
1. Validate `team_members` row exists
2. Resolve team and event from the row
3. Validate caller is `LEADER` of this team
4. Validate target is not caller themselves
5. Validate target `status = 'accepted'`
6. Validate event `status = 'registration_open'`
7. Validate team not `eliminated`/`disqualified`
8. Validate `team.locked_at IS NULL`
9. Validate new role is different from current role (guard no-op)
10. If `newRole = LEADER`: atomically update current leader → `MEMBER`, then target → `LEADER`
11. If `newRole = MEMBER`: update target → `MEMBER`
12. Write `audit_logs` entry
13. Return `200 TeamMemberResponse`

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 400 | `INVALID_ROLE` | `role` not a valid enum value |
| 403 | `FORBIDDEN_NOT_LEADER` | Caller is not the team leader |
| 404 | `TEAM_MEMBER_NOT_FOUND` | `teamMemberId` does not exist |
| 409 | `CANNOT_CHANGE_OWN_ROLE` | Target is the caller |
| 409 | `MEMBER_STATUS_INVALID` | Target member is not `accepted` |
| 409 | `REGISTRATION_NOT_OPEN` | Registration is closed |
| 409 | `TEAM_TERMINATED` | Team is eliminated or disqualified |
| 409 | `TEAM_LOCKED` | Team is locked |

---

## 10. PATCH `/team-members/{teamMemberId}/accept`

### Purpose
Accept a pending invitation. Only the invited user themselves may call this.

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `teamMemberId` | UUID | Yes | Target team_members row ID |

### Request Body
None.

### Response DTO — `200 OK` — `TeamMemberResponse`
Returns the updated member row with `status = "ACCEPTED"` and `acceptedAt` set.

### Business Flow
1. Validate `team_members` row exists
2. Resolve team and event from the row
3. Validate caller is the user referenced by the row
4. Validate current `status = 'invited'`
5. Validate event `status = 'registration_open'`
6. Validate team not `eliminated`/`disqualified`
7. Validate `team.locked_at IS NULL`
8. Validate team accepted count < `max_team_size` (edge: team was filled between invite and accept)
9. Update `status = 'accepted'`, `accepted_at = now()`
10. DB trigger fires `sync_team_status()` — team may advance status
11. Write `audit_logs` entry
12. Return `200 TeamMemberResponse`

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 403 | `FORBIDDEN_NOT_MEMBER_OWNER` | Caller is not the invited user |
| 404 | `TEAM_MEMBER_NOT_FOUND` | `teamMemberId` does not exist |
| 409 | `MEMBER_STATUS_INVALID` | Current status is not `invited` |
| 409 | `REGISTRATION_NOT_OPEN` | Registration is closed |
| 409 | `TEAM_TERMINATED` | Team is eliminated or disqualified |
| 409 | `TEAM_LOCKED` | Team is locked |
| 409 | `TEAM_FULL` | All accepted slots already filled |

---

## 11. PATCH `/team-members/{teamMemberId}/decline`

### Purpose
Decline a pending invitation. Only the invited user themselves may call this. Declining is always permitted regardless of registration status — the user is freeing a slot.

### Path Parameters
| Param | Type | Required | Description |
|---|---|---|---|
| `teamMemberId` | UUID | Yes | Target team_members row ID |

### Request Body
None.

### Response DTO — `200 OK` — `TeamMemberResponse`
Returns the updated member row with `status = "DECLINED"` and `declinedAt` set.

### Business Flow
1. Validate `team_members` row exists
2. Validate caller is the user referenced by the row
3. Validate current `status = 'invited'`
4. Update `status = 'declined'`, `declined_at = now()`
5. DB trigger fires `sync_team_status()` — team may regress status
6. Write `audit_logs` entry
7. Return `200 TeamMemberResponse`

### Possible Errors
| Status | Error Code | Condition |
|---|---|---|
| 403 | `FORBIDDEN_NOT_MEMBER_OWNER` | Caller is not the invited user |
| 404 | `TEAM_MEMBER_NOT_FOUND` | `teamMemberId` does not exist |
| 409 | `MEMBER_STATUS_INVALID` | Current status is not `invited` |

---

## Appendix: DTO Summary

```
CreateTeamRequest        { name, description }
UpdateTeamRequest        { name, description }
InviteMemberRequest      { userId }
ChangeRoleRequest        { role }

TeamSummaryResponse      { id, name, status, registrationMode, memberCount, maxMemberCount, minMemberCount, isLocked, approvedAt, createdAt }
TeamDetailResponse       { id, eventId, teamProfileId, name, description, status, registrationMode, isLocked, lockedReason, minMemberCount, maxMemberCount, approvedAt, createdBy, createdAt, members[] }
TeamMemberResponse       { id, teamId, userId, fullName, email, studentType, role, status, joinedAt, acceptedAt, declinedAt, removedAt }
PagedResponse<T>         { content[], page, size, totalElements, totalPages }
ErrorResponse            { timestamp, status, error, message, path }
```