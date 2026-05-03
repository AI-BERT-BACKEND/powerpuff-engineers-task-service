-- V1__create_tasks_table.sql
-- Tabla principal del microservicio task-service

CREATE TABLE IF NOT EXISTS tasks (
    id                          VARCHAR(36)  NOT NULL,
    student_id                  VARCHAR(255) NOT NULL,
    subject_id                  VARCHAR(255) NOT NULL,
    title                       VARCHAR(255) NOT NULL,
    description                 TEXT,
    estimated_duration_minutes  INTEGER      NOT NULL,
    deadline                    TIMESTAMP    NOT NULL,
    priority                    VARCHAR(20)  NOT NULL,
    status                      VARCHAR(20)  NOT NULL DEFAULT 'TODO',
    scheduled_date              TIMESTAMP,
    completed_at                TIMESTAMP,

    CONSTRAINT pk_tasks PRIMARY KEY (id),
    CONSTRAINT uq_student_subject_title UNIQUE (student_id, subject_id, title),
    CONSTRAINT chk_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_status   CHECK (status   IN ('TODO', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT chk_duration CHECK (estimated_duration_minutes > 0)
);

CREATE INDEX IF NOT EXISTS idx_tasks_student_id ON tasks (student_id);
CREATE INDEX IF NOT EXISTS idx_tasks_deadline   ON tasks (deadline);
CREATE INDEX IF NOT EXISTS idx_tasks_status     ON tasks (status);
