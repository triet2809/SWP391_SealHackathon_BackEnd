-- Gap-fill migration #4:
--   * track team-capacity limit (coordinator-configurable)
--   * per-user notifications (red-dot / unread counts)
-- Additive only; safe under ddl-auto=validate.

BEGIN;

-- tracks: optional max number of teams. NULL = unlimited.
ALTER TABLE tracks ADD COLUMN IF NOT EXISTS max_teams INTEGER;

-- notifications: one row per (recipient, event). Read state via read_at.
CREATE TABLE IF NOT EXISTS notifications (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         VARCHAR(40)  NOT NULL,
    title        VARCHAR(255) NOT NULL,
    body         TEXT,
    -- Logical grouping so the FE can map a notification to a sidebar feature
    -- (e.g. 'submissions', 'join_requests', 'support', 'notices').
    category     VARCHAR(40)  NOT NULL,
    -- Optional deep-link target (entity id the notification is about).
    ref_type     VARCHAR(40),
    ref_id       UUID,
    read_at      TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications (user_id);
-- Fast "unread by category" lookups that power the red dots.
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread
    ON notifications (user_id, category) WHERE read_at IS NULL;

COMMIT;
