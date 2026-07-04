-- Gap-fill migration #5:
--   * real submission lifecycle status (draft vs submitted)
-- Before this, the FE "Draft/Submitted" label was cosmetic (based only on
-- row existence) and the Save Draft / Submit buttons did the same thing.
-- Additive only; safe under ddl-auto=validate.

BEGIN;

-- submissions.status: 'draft' = work in progress (not yet handed in),
-- 'submitted' = officially handed in to reviewers.
ALTER TABLE submissions ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'draft';

-- Existing rows predate the concept; treat them as already submitted so we
-- don't silently "un-submit" anyone who already handed in.
UPDATE submissions SET status = 'submitted' WHERE status = 'draft';

-- New rows created going forward default to 'draft' at the DB level; the
-- service sets the correct value explicitly on each write.

COMMIT;
