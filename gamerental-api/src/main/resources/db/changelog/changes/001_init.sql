--liquibase formatted sql

--changeset system:001-init

CREATE TABLE app_users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE games (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title        VARCHAR(255) NOT NULL,
    description  TEXT         NOT NULL DEFAULT '',
    total_copies INT          NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE game_tags (
    game_id UUID        NOT NULL REFERENCES games (id) ON DELETE CASCADE,
    tag     VARCHAR(50) NOT NULL,
    PRIMARY KEY (game_id, tag)
);

CREATE TABLE game_copies (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id      UUID        NOT NULL REFERENCES games (id) ON DELETE CASCADE,
    copy_number  INT         NOT NULL,
    condition    VARCHAR(20) NOT NULL DEFAULT 'NEW',
    is_available BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (game_id, copy_number)
);

CREATE TABLE clients (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    phone      VARCHAR(50),
    email      VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE rentals (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    game_copy_id         UUID        NOT NULL REFERENCES game_copies (id),
    client_id            UUID        NOT NULL REFERENCES clients (id),
    rented_from          DATE        NOT NULL,
    due_to               DATE        NOT NULL,
    returned_at          DATE,
    condition_on_return  VARCHAR(20),
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes                TEXT,
    created_at           TIMESTAMP   NOT NULL DEFAULT NOW()
);

