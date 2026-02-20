-- ============================================================
-- V1__create_schema.sql
-- Flyway migration — initial schema for Fitness App
-- ============================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- -------------------------------------------------------
-- ENUM types
-- -------------------------------------------------------

CREATE TYPE goal_type AS ENUM (
    'TARGET_WEIGHT',
    'WORKOUT_FREQUENCY'
);

-- -------------------------------------------------------
-- users
-- -------------------------------------------------------

CREATE TABLE users (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    firebase_uid    VARCHAR(128) NOT NULL,
    email           VARCHAR(320) NOT NULL,
    display_name    VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT users_pk              PRIMARY KEY (id),
    CONSTRAINT users_firebase_uid_uk UNIQUE (firebase_uid)
);

CREATE INDEX users_email_idx ON users (email);

-- -------------------------------------------------------
-- muscle_groups
-- -------------------------------------------------------

CREATE TABLE muscle_groups (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description TEXT,

    CONSTRAINT muscle_groups_pk      PRIMARY KEY (id),
    CONSTRAINT muscle_groups_name_uk UNIQUE (name)
);

-- -------------------------------------------------------
-- exercises
-- -------------------------------------------------------

CREATE TABLE exercises (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid(),
    name                 VARCHAR(255) NOT NULL,
    description          TEXT,
    muscle_group_id      UUID         NOT NULL,
    instructions         TEXT,
    created_by_user_id   UUID,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT exercises_pk              PRIMARY KEY (id),
    CONSTRAINT exercises_muscle_group_fk FOREIGN KEY (muscle_group_id)
        REFERENCES muscle_groups (id) ON DELETE RESTRICT,
    CONSTRAINT exercises_created_by_fk   FOREIGN KEY (created_by_user_id)
        REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX exercises_muscle_group_id_idx    ON exercises (muscle_group_id);
CREATE INDEX exercises_created_by_user_id_idx ON exercises (created_by_user_id);
CREATE INDEX exercises_name_search_idx        ON exercises (LOWER(name));

-- -------------------------------------------------------
-- workout_sessions
-- -------------------------------------------------------

CREATE TABLE workout_sessions (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL,
    name         VARCHAR(255),
    started_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    notes        TEXT,

    CONSTRAINT workout_sessions_pk      PRIMARY KEY (id),
    CONSTRAINT workout_sessions_user_fk FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX workout_sessions_user_id_idx      ON workout_sessions (user_id);
CREATE INDEX workout_sessions_user_started_idx ON workout_sessions (user_id, started_at DESC);

-- -------------------------------------------------------
-- workout_sets
-- -------------------------------------------------------

CREATE TABLE workout_sets (
    id           UUID           NOT NULL DEFAULT gen_random_uuid(),
    session_id   UUID           NOT NULL,
    exercise_id  UUID           NOT NULL,
    set_number   INT            NOT NULL,
    reps         INT            NOT NULL,
    weight_kg    DECIMAL(6, 2)  NOT NULL,
    completed_at TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT workout_sets_pk                  PRIMARY KEY (id),
    CONSTRAINT workout_sets_session_fk          FOREIGN KEY (session_id)
        REFERENCES workout_sessions (id) ON DELETE CASCADE,
    CONSTRAINT workout_sets_exercise_fk         FOREIGN KEY (exercise_id)
        REFERENCES exercises (id) ON DELETE RESTRICT,
    CONSTRAINT workout_sets_set_number_positive CHECK (set_number > 0),
    CONSTRAINT workout_sets_reps_positive       CHECK (reps > 0),
    CONSTRAINT workout_sets_weight_non_negative CHECK (weight_kg >= 0)
);

CREATE INDEX workout_sets_session_id_idx  ON workout_sets (session_id);
CREATE INDEX workout_sets_exercise_id_idx ON workout_sets (exercise_id);

-- -------------------------------------------------------
-- nutrition_logs
-- -------------------------------------------------------

CREATE TABLE nutrition_logs (
    id         UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id    UUID          NOT NULL,
    logged_at  DATE          NOT NULL,
    meal_name  VARCHAR(255)  NOT NULL,
    calories   DECIMAL(8, 2) NOT NULL,
    protein_g  DECIMAL(8, 2) NOT NULL,
    carbs_g    DECIMAL(8, 2) NOT NULL,
    fat_g      DECIMAL(8, 2) NOT NULL,
    notes      TEXT,

    CONSTRAINT nutrition_logs_pk                    PRIMARY KEY (id),
    CONSTRAINT nutrition_logs_user_fk               FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT nutrition_logs_calories_non_negative CHECK (calories >= 0),
    CONSTRAINT nutrition_logs_protein_non_negative  CHECK (protein_g >= 0),
    CONSTRAINT nutrition_logs_carbs_non_negative    CHECK (carbs_g >= 0),
    CONSTRAINT nutrition_logs_fat_non_negative      CHECK (fat_g >= 0)
);

CREATE INDEX nutrition_logs_user_id_idx   ON nutrition_logs (user_id);
CREATE INDEX nutrition_logs_user_date_idx ON nutrition_logs (user_id, logged_at DESC);

-- -------------------------------------------------------
-- goals
-- -------------------------------------------------------

CREATE TABLE goals (
    id           UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id      UUID          NOT NULL,
    goal_type    goal_type     NOT NULL,
    target_value DECIMAL(10,2) NOT NULL,
    unit         VARCHAR(50)   NOT NULL,
    target_date  DATE,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT goals_pk      PRIMARY KEY (id),
    CONSTRAINT goals_user_fk FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX goals_user_id_idx ON goals (user_id);

-- -------------------------------------------------------
-- notification_preferences
-- -------------------------------------------------------

CREATE TABLE notification_preferences (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id           UUID         NOT NULL,
    fcm_token         VARCHAR(512) NOT NULL,
    reminders_enabled BOOLEAN      NOT NULL DEFAULT false,
    reminder_time     TIME         NOT NULL DEFAULT '08:00',
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT notification_preferences_pk      PRIMARY KEY (id),
    CONSTRAINT notification_preferences_user_uk UNIQUE (user_id),
    CONSTRAINT notification_preferences_user_fk FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);
