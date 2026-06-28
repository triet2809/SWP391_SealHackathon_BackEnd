# TEAM_FLOW.md — Team Management Flow

> Scope: BE3 — Team Management API
>
> Jira: Team - Create Team & Member Management API

---

# 1. Create Team

Endpoint

```http
POST /events/{eventId}/teams
```

Flow

1. Extract authenticated userId.

2. Verify user account is approved.

   If not approved

```text
403 FORBIDDEN

Account not approved
```

3. Verify event exists.

   If event not found

```text
404 NOT FOUND

Event not found
```

4. Validate registration is open.

Conditions:

* registration_start_at <= now
* registration_end_at >= now
* registration_closed_at IS NULL

If validation fails

```text
409 CONFLICT

Registration is not open
```

5. Verify user is not already participating in another team for the same event.

If already participating

```text
409 CONFLICT

User already in a team for this event
```

6. Validate team name uniqueness inside event.

(case-insensitive)

If duplicated

```text
409 CONFLICT

Team name already taken
```

7. Create team.

```text
teams

status = waiting_for_members

created_by = currentUserId
```

8. Create first member automatically.

```text
team_members

role = LEADER

status = accepted

accepted_at = now
```

9. Database trigger updates team status.

10. Return

```text
201 CREATED
```

Response

```text
TeamDetailResponse
```

---

# 2. Invite Member

Endpoint

```http
POST /teams/{teamId}/members/invite
```

Flow

1. Extract authenticated userId.

2. Verify team exists.

If not found

```text
404 NOT FOUND
```

3. Verify caller is leader.

If not

```text
403 FORBIDDEN

Only leader can invite members
```

4. Validate registration is open.

If closed

```text
409 CONFLICT
```

5. Verify team is not eliminated.

6. Verify team is not disqualified.

If terminated

```text
409 CONFLICT

Team is terminated
```

7. Verify team is not locked.

```text
locked_at IS NULL
```

If locked

```text
409 CONFLICT

Team is locked
```

8. Validate team capacity.

```text
invited + accepted < max_team_size
```

If full

```text
409 CONFLICT

Team is full
```

9. Verify target account is approved.

If not

```text
422 UNPROCESSABLE ENTITY
```

10. Verify target user is not already in another team.

If already assigned

```text
409 CONFLICT
```

11. Create membership.

```text
role = MEMBER

status = invited
```

12. Database trigger synchronizes team status.

13. Return

```text
201 CREATED
```

---

# 3. Accept Invitation

Endpoint

```http
PATCH /team-members/{teamMemberId}/accept
```

Flow

1. Extract authenticated userId.

2. Verify membership exists.

If not

```text
404 NOT FOUND
```

3. Verify caller owns invitation.

If not

```text
403 FORBIDDEN
```

4. Verify current status is INVITED.

If not

```text
409 CONFLICT
```

5. Validate registration is open.

If closed

```text
409 CONFLICT
```

6. Verify team is not eliminated.

7. Verify team is not disqualified.

8. Verify team is not locked.

If locked

```text
409 CONFLICT
```

9. Validate available slots.

```text
accepted_members < max_team_size
```

If full

```text
409 CONFLICT
```

10. Update member.

```text
status = accepted

accepted_at = now
```

11. Database trigger updates team status.

12. Return

```text
200 OK
```

---

# 4. Decline Invitation

Endpoint

```http
PATCH /team-members/{teamMemberId}/decline
```

Flow

1. Extract authenticated userId.

2. Verify membership exists.

3. Verify caller owns invitation.

4. Verify current status equals INVITED.

If not

```text
409 CONFLICT
```

5. Update membership.

```text
status = declined

declined_at = now
```

6. Database trigger synchronizes team status.

7. Return

```text
200 OK
```

Response

```text
TeamMemberResponse
```

---

# 5. Delete Team Member

Endpoint

```http
DELETE /team-members/{teamMemberId}
```

Flow

1. Extract authenticated userId.

2. Verify membership exists.

3. Verify caller is team leader.

If not

```text
403 FORBIDDEN
```

4. Verify target member is not the leader.

If leader

```text
409 CONFLICT
```

5. Verify target member status.

Allowed

```text
INVITED

ACCEPTED
```

Rejected

```text
DECLINED

REMOVED
```

6. Validate registration is open.

7. Verify team is not eliminated.

8. Verify team is not disqualified.

9. Verify team is not locked.

10. Update member.

```text
status = removed

removed_at = now
```

11. Database trigger updates team status.

12. Return

```text
204 NO CONTENT
```

---

# 6. Change Member Role

Endpoint

```http
PATCH /team-members/{teamMemberId}/role
```

Flow

1. Extract authenticated userId.

2. Verify membership exists.

3. Verify caller is leader.

If not

```text
403 FORBIDDEN
```

4. Verify caller is not changing their own role.

If attempting

```text
409 CONFLICT
```

5. Verify target member is ACCEPTED.

If not

```text
409 CONFLICT
```

6. Validate registration is open.

7. Verify team is not eliminated.

8. Verify team is not disqualified.

9. Verify team is not locked.

10. Update role.

If transferring leadership

```text
current leader

LEADER -> MEMBER

target member

MEMBER -> LEADER
```

Otherwise

```text
target.role = requestedRole
```

11. Return

```text
200 OK
```

---

# 7. Team Status Reference

Possible statuses

```text
waiting_for_members

approved_open

approved_full

eliminated

disqualified
```

Transitions

```text
Create Team
↓

waiting_for_members

Enough accepted members
↓

approved_open

Reached max size
↓

approved_full

Coordinator action
↓

eliminated

or

disqualified
```

---

# 8. Member Status Reference

Possible statuses

```text
invited

accepted

declined

removed
```

Transitions

```text
Leader invites user
↓

invited

Accept
↓

accepted

Decline
↓

declined

Remove
↓

removed
```
