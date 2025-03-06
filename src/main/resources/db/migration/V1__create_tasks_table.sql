CREATE TABLE tasks
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title       VARCHAR(50)                             NOT NULL,
    description VARCHAR(100)                            NOT NULL,
    completed   BOOLEAN                                 NOT NULL,
    due_date    TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    CONSTRAINT pk_tasks PRIMARY KEY (id)
);