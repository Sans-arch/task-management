ALTER TABLE tasks
    ADD COLUMN owner_id UUID NOT NULL REFERENCES users(id);

CREATE INDEX idx_tasks_owner_id ON tasks(owner_id);
