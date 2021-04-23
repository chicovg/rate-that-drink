CREATE TABLE drink (
    id SERIAL PRIMARY KEY,
    user_id          INTEGER NOT NULL,
    name             VARCHAR(100) NOT NULL,
    maker            VARCHAR(100),
    type             VARCHAR(10),
    style            VARCHAR(100),
    appearance       SMALLINT,
    appearance_notes TEXT,
    smell            SMALLINT,
    smell_notes      TEXT,
    taste            SMALLINT,
    taste_notes      TEXT,
    comments         TEXT,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);
