-- V4__add_status_changed_at_to_tasks.sql
-- Tracks the last time a task's status was changed.

ALTER TABLE tasks
    ADD COLUMN status_changed_at TIMESTAMP;
