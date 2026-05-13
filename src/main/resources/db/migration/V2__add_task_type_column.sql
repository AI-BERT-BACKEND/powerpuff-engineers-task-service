-- V2__add_task_type_column.sql
-- Agrega el campo task_type (requerimiento R11) y hace estimated_duration_minutes opcional

ALTER TABLE tasks
    ADD COLUMN task_type VARCHAR(20);

-- Asignar valor por defecto a filas existentes antes de aplicar NOT NULL
UPDATE tasks SET task_type = 'TAREA' WHERE task_type IS NULL;

ALTER TABLE tasks
    ALTER COLUMN task_type SET NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT chk_task_type CHECK (task_type IN ('TAREA', 'EXAMEN', 'PROYECTO', 'LECTURA', 'OTRO'));

-- estimated_duration_minutes pasa a ser opcional (puede ser NULL)
ALTER TABLE tasks
    ALTER COLUMN estimated_duration_minutes DROP NOT NULL;

-- Reemplazar constraint de duración para permitir NULL
ALTER TABLE tasks
    DROP CONSTRAINT IF EXISTS chk_duration;

ALTER TABLE tasks
    ADD CONSTRAINT chk_duration CHECK (estimated_duration_minutes IS NULL OR estimated_duration_minutes > 0);

CREATE INDEX IF NOT EXISTS idx_tasks_task_type ON tasks (task_type);
