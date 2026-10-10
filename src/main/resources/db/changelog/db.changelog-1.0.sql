--liquibase formatted sql
--changeset mluvse:1
CREATE TABLE IF NOT EXISTS users
(
    id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(250) NOT NULL ,
    surname VARCHAR(250) NOT NULL ,
    birth_date DATE,
    email VARCHAR(250) NOT NULL ,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_cards
(
    id BIGSERIAL PRIMARY KEY ,
    user_id BIGINT NOT NULL REFERENCES users(id),
    number VARCHAR(250) NOT NULL ,
    holder VARCHAR(250) NOT NULL ,
    expiration_date DATE NOT NULL ,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users (email);

CREATE INDEX IF NOT EXISTS idx_payment_cards_user_id ON payment_cards (user_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_cards_number ON payment_cards (number);