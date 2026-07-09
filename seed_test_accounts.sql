-- ============================================================================
-- Seed test accounts for manual feature testing
-- Query-tool friendly (pgAdmin / DBeaver / psql). Plain SQL, no COPY.
-- Idempotent: safe to run multiple times (INSERT ... WHERE NOT EXISTS).
--
-- All accounts password = 11111111
-- (bcrypt hash reused from existing verified seed account test.ec@seal.local)
--
-- Accounts created:
--   demo.ec@seal.local      -> coordinator (EC)
--   demo.mentor@seal.local  -> mentor (assigned to demo track)
--   demo.judge@seal.local   -> judge  (assigned to demo track + round)
--   demo.leader@seal.local  -> team_leader, already in a team (as leader)
--   demo.member@seal.local  -> team_member, already in the same team
--
-- Supporting data: one demo event -> track -> round -> team.
-- ============================================================================

BEGIN;

-- Common bcrypt(12) hash for password "11111111"
-- $2a$12$lbyENCECafzqrU3En/KFHuazqdU/NWwLVtlME.hV5reCdFxuBd45S

-- ---------------------------------------------------------------------------
-- 1) Users
-- ---------------------------------------------------------------------------
INSERT INTO public.users (id, email, password_hash, full_name, student_type, is_guest, status, created_at)
SELECT 'a1000000-0000-4000-8000-000000000001', 'demo.ec@seal.local',
       '$2a$12$lbyENCECafzqrU3En/KFHuazqdU/NWwLVtlME.hV5reCdFxuBd45S',
       'Demo Coordinator', 'none', false, 'approved', now()
WHERE NOT EXISTS (SELECT 1 FROM public.users WHERE email = 'demo.ec@seal.local');

INSERT INTO public.users (id, email, password_hash, full_name, student_type, is_guest, status, created_at)
SELECT 'a1000000-0000-4000-8000-000000000002', 'demo.mentor@seal.local',
       '$2a$12$lbyENCECafzqrU3En/KFHuazqdU/NWwLVtlME.hV5reCdFxuBd45S',
       'Demo Mentor', 'none', false, 'approved', now()
WHERE NOT EXISTS (SELECT 1 FROM public.users WHERE email = 'demo.mentor@seal.local');

INSERT INTO public.users (id, email, password_hash, full_name, student_type, is_guest, status, created_at)
SELECT 'a1000000-0000-4000-8000-000000000003', 'demo.judge@seal.local',
       '$2a$12$lbyENCECafzqrU3En/KFHuazqdU/NWwLVtlME.hV5reCdFxuBd45S',
       'Demo Judge', 'none', false, 'approved', now()
WHERE NOT EXISTS (SELECT 1 FROM public.users WHERE email = 'demo.judge@seal.local');

INSERT INTO public.users (id, email, password_hash, full_name, student_type, student_id, is_guest, status, created_at)
SELECT 'a1000000-0000-4000-8000-000000000004', 'demo.leader@seal.local',
       '$2a$12$lbyENCECafzqrU3En/KFHuazqdU/NWwLVtlME.hV5reCdFxuBd45S',
       'Demo Team Leader', 'fpt', 'SE100004', false, 'approved', now()
WHERE NOT EXISTS (SELECT 1 FROM public.users WHERE email = 'demo.leader@seal.local');

INSERT INTO public.users (id, email, password_hash, full_name, student_type, student_id, is_guest, status, created_at)
SELECT 'a1000000-0000-4000-8000-000000000005', 'demo.member@seal.local',
       '$2a$12$lbyENCECafzqrU3En/KFHuazqdU/NWwLVtlME.hV5reCdFxuBd45S',
       'Demo Team Member', 'fpt', 'SE100005', false, 'approved', now()
WHERE NOT EXISTS (SELECT 1 FROM public.users WHERE email = 'demo.member@seal.local');

-- ---------------------------------------------------------------------------
-- 2) Role assignments (roles table seeded by app; matched by name)
-- ---------------------------------------------------------------------------
INSERT INTO public.user_roles (user_id, role_id, assigned_at)
SELECT u.id, r.id, now()
FROM public.users u, public.roles r
WHERE (u.email, r.name) IN (
        ('demo.ec@seal.local',     'coordinator'),
        ('demo.mentor@seal.local', 'mentor'),
        ('demo.judge@seal.local',  'judge'),
        ('demo.leader@seal.local', 'team_leader'),
        ('demo.member@seal.local', 'team_member')
      )
  AND NOT EXISTS (
        SELECT 1 FROM public.user_roles ur
        WHERE ur.user_id = u.id AND ur.role_id = r.id
      );

-- ---------------------------------------------------------------------------
-- 3) Demo event -> track -> round
-- ---------------------------------------------------------------------------
INSERT INTO public.events (id, title, description, status, term, prize_pool,
                           registration_start, registration_end, event_start, event_end, created_at)
SELECT 'aaaa0000-0000-4000-8000-000000000001', 'DEMO Event (test accounts)',
       'Seed event for manual feature testing.', 'ongoing', 'Summer 2026', '10.000.000 VND',
       now() - interval '7 days', now() + interval '7 days',
       now() - interval '1 day',  now() + interval '30 days', now()
WHERE NOT EXISTS (SELECT 1 FROM public.events WHERE id = 'aaaa0000-0000-4000-8000-000000000001');

INSERT INTO public.tracks (id, event_id, name, description, max_teams, created_at)
SELECT 'aaaa0000-0000-4000-8000-000000000002', 'aaaa0000-0000-4000-8000-000000000001',
       'DEMO Track', 'Seed track for manual testing.', 20, now()
WHERE NOT EXISTS (SELECT 1 FROM public.tracks WHERE id = 'aaaa0000-0000-4000-8000-000000000002');

INSERT INTO public.rounds (id, track_id, name, sequence_number, submission_deadline, top_n_to_promote, created_at)
SELECT 'aaaa0000-0000-4000-8000-000000000003', 'aaaa0000-0000-4000-8000-000000000002',
       'DEMO Round 1', 1, now() + interval '14 days', 3, now()
WHERE NOT EXISTS (SELECT 1 FROM public.rounds WHERE id = 'aaaa0000-0000-4000-8000-000000000003');

-- ---------------------------------------------------------------------------
-- 4) Team with leader + member
-- ---------------------------------------------------------------------------
INSERT INTO public.teams (id, track_id, name, status, invite_code, created_at)
SELECT 'aaaa0000-0000-4000-8000-000000000004', 'aaaa0000-0000-4000-8000-000000000002',
       'DEMO Team', 'active', 'DEMO2026', now()
WHERE NOT EXISTS (SELECT 1 FROM public.teams WHERE id = 'aaaa0000-0000-4000-8000-000000000004');

INSERT INTO public.team_members (id, team_id, user_id, role, joined_at)
SELECT 'a2000000-0000-4000-8000-000000000001', 'aaaa0000-0000-4000-8000-000000000004',
       (SELECT id FROM public.users WHERE email = 'demo.leader@seal.local'), 'leader', now()
WHERE NOT EXISTS (
        SELECT 1 FROM public.team_members
        WHERE team_id = 'aaaa0000-0000-4000-8000-000000000004'
          AND user_id = (SELECT id FROM public.users WHERE email = 'demo.leader@seal.local'));

INSERT INTO public.team_members (id, team_id, user_id, role, joined_at)
SELECT 'a2000000-0000-4000-8000-000000000002', 'aaaa0000-0000-4000-8000-000000000004',
       (SELECT id FROM public.users WHERE email = 'demo.member@seal.local'), 'member', now()
WHERE NOT EXISTS (
        SELECT 1 FROM public.team_members
        WHERE team_id = 'aaaa0000-0000-4000-8000-000000000004'
          AND user_id = (SELECT id FROM public.users WHERE email = 'demo.member@seal.local'));

-- ---------------------------------------------------------------------------
-- 5) Assign mentor + judge to the demo track/round
-- ---------------------------------------------------------------------------
INSERT INTO public.track_mentors (id, event_id, track_id, user_id, assigned_at)
SELECT 'a3000000-0000-4000-8000-000000000001', 'aaaa0000-0000-4000-8000-000000000001',
       'aaaa0000-0000-4000-8000-000000000002',
       (SELECT id FROM public.users WHERE email = 'demo.mentor@seal.local'), now()
WHERE NOT EXISTS (
        SELECT 1 FROM public.track_mentors
        WHERE track_id = 'aaaa0000-0000-4000-8000-000000000002'
          AND user_id = (SELECT id FROM public.users WHERE email = 'demo.mentor@seal.local'));

INSERT INTO public.track_judges (id, event_id, track_id, user_id, assigned_at)
SELECT 'a3000000-0000-4000-8000-000000000002', 'aaaa0000-0000-4000-8000-000000000001',
       'aaaa0000-0000-4000-8000-000000000002',
       (SELECT id FROM public.users WHERE email = 'demo.judge@seal.local'), now()
WHERE NOT EXISTS (
        SELECT 1 FROM public.track_judges
        WHERE track_id = 'aaaa0000-0000-4000-8000-000000000002'
          AND user_id = (SELECT id FROM public.users WHERE email = 'demo.judge@seal.local'));

INSERT INTO public.round_judges (id, round_id, user_id, assigned_at)
SELECT 'a3000000-0000-4000-8000-000000000003', 'aaaa0000-0000-4000-8000-000000000003',
       (SELECT id FROM public.users WHERE email = 'demo.judge@seal.local'), now()
WHERE NOT EXISTS (
        SELECT 1 FROM public.round_judges
        WHERE round_id = 'aaaa0000-0000-4000-8000-000000000003'
          AND user_id = (SELECT id FROM public.users WHERE email = 'demo.judge@seal.local'));

COMMIT;

-- ---------------------------------------------------------------------------
-- Verify
-- ---------------------------------------------------------------------------
-- SELECT u.email, r.name AS role, u.status
-- FROM public.users u
-- JOIN public.user_roles ur ON ur.user_id = u.id
-- JOIN public.roles r ON r.id = ur.role_id
-- WHERE u.email LIKE 'demo.%@seal.local'
-- ORDER BY u.email;
