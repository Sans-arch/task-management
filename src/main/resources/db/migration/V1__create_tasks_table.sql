CREATE TABLE tasks (
    id          UUID        NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(50)  NOT NULL,
    priority    VARCHAR(50)  NOT NULL,
    due_date    DATE,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,

    CONSTRAINT pk_tasks PRIMARY KEY (id)
);
