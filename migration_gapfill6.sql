-- Gap-fill migration #5 (demo-critical workflows):
--   * Team Journey timeline (team_timeline_events)
--   * Cua so khieu nai 15 phut (appeals + cot moi tren rounds)
--   * Quyet dinh phan dinh hoa thu cong (tie_break_decisions)
--   * The le su kien + xac nhan chap thuan (event_rules, rule_acceptances)
--   * Lich su chinh sua giai thuong (prize_revisions)
-- Additive only; safe under ddl-auto=validate.

BEGIN;

-- rounds: thoi diem cong bo ket qua + han chot khieu nai (cong bo + 15 phut)
ALTER TABLE rounds ADD COLUMN IF NOT EXISTS result_published_at TIMESTAMP;
ALTER TABLE rounds ADD COLUMN IF NOT EXISTS appeal_deadline TIMESTAMP;

-- Dong thoi gian (timeline) hanh trinh cua doi — append-only
CREATE TABLE IF NOT EXISTS team_timeline_events (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID          NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    team_id         UUID          NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    round_id        UUID          REFERENCES rounds(id) ON DELETE SET NULL,
    type            VARCHAR(40)   NOT NULL,
    title           VARCHAR(255)  NOT NULL,
    description     TEXT,
    score_snapshot  NUMERIC(10,2),
    rank_snapshot   INTEGER,
    status_snapshot VARCHAR(40),
    occurred_at     TIMESTAMP     NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_team_timeline_events_team ON team_timeline_events (team_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_team_timeline_events_event ON team_timeline_events (event_id, occurred_at DESC);

-- Don khieu nai ket qua vong thi (cua so 15 phut)
CREATE TABLE IF NOT EXISTS appeals (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id            UUID         NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    round_id            UUID         NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    team_id             UUID         NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    submitted_by        UUID         NOT NULL REFERENCES users(id),
    reason              TEXT         NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    response            TEXT,
    resolved_by         UUID         REFERENCES users(id),
    result_published_at TIMESTAMP,
    appeal_deadline     TIMESTAMP,
    resolved_at         TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_appeals_round ON appeals (round_id);
CREATE INDEX IF NOT EXISTS idx_appeals_team ON appeals (team_id);
-- Truy van "con khieu nai PENDING trong vong?" phai nhanh (chan thang hang)
CREATE INDEX IF NOT EXISTS idx_appeals_round_status ON appeals (round_id, status);

-- Quyet dinh phan dinh hoa thu cong sau khi review ma nguon GitHub
CREATE TABLE IF NOT EXISTS tie_break_decisions (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    round_id     UUID         NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    team_id      UUID         NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    decided_by   UUID         NOT NULL REFERENCES users(id),
    decided_at   TIMESTAMP    NOT NULL DEFAULT now(),
    reason       TEXT         NOT NULL,
    evidence_url VARCHAR(500),
    note         TEXT,
    CONSTRAINT uq_tie_break_decisions_round_team UNIQUE (round_id, team_id)
);

-- The le su kien voi 3 muc hien thi: PUBLIC / INTERNAL / DISPUTE_ONLY
CREATE TABLE IF NOT EXISTS event_rules (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id      UUID         NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    title         VARCHAR(255) NOT NULL,
    content       TEXT         NOT NULL,
    visibility    VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
    display_order INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_event_rules_event ON event_rules (event_id, visibility);

-- Ghi nhan viec nguoi dung chap nhan the le cua su kien (1 ban ghi / user / event)
CREATE TABLE IF NOT EXISTS rule_acceptances (
    id          UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id    UUID      NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    accepted_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_rule_acceptances_user_event UNIQUE (user_id, event_id)
);

-- Lich su thu hoi / chuyen giai thuong — khong bao gio xoa cung
CREATE TABLE IF NOT EXISTS prize_revisions (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    prize_id      UUID        NOT NULL REFERENCES prizes(id) ON DELETE CASCADE,
    action        VARCHAR(20) NOT NULL,
    old_team_id   UUID        REFERENCES teams(id) ON DELETE SET NULL,
    new_team_id   UUID        REFERENCES teams(id) ON DELETE SET NULL,
    reason        TEXT        NOT NULL,
    evidence_note TEXT,
    changed_by    UUID        NOT NULL REFERENCES users(id),
    changed_at    TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_prize_revisions_prize ON prize_revisions (prize_id, changed_at DESC);

COMMIT;
