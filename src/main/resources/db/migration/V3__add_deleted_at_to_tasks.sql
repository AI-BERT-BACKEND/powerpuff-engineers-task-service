-- V3__add_deleted_at_to_tasks.sql
-- Adds soft-delete support: tasks are marked as deleted instead of being physically removed.

ALTER TABLE tasks
    ADD COLUMN deleted_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_tasks_deleted_at ON tasks (deleted_at);
