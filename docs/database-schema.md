# Fitness App — PostgreSQL Database Schema

## Overview

All user-generated data persists in PostgreSQL 16. The schema follows these conventions:
- UUID primary keys (`gen_random_uuid()`) for distributed-safe IDs
- `snake_case` column names
- `TIMESTAMPTZ` for all timestamps (UTC storage)
- Soft-delete not used — hard delete with cascades where appropriate
- `updated_at` triggers managed at application layer (R2DBC)

---

## Entity-Relationship Diagram

```mermaid
erDiagram
    users {
        uuid id PK
        varchar firebase_uid UK
        varchar email
        varchar display_name
        timestamptz created_at
        timestamptz updated_at
    }

    muscle_groups {
        uuid id PK
        varchar name UK
        text description
    }

    exercises {
        uuid id PK
        varchar name
        text description
        uuid muscle_group_id FK
        text instructions
        uuid created_by_user_id FK
        timestamptz created_at
    }

    workout_sessions {
        uuid id PK
        uuid user_id FK
        varchar name
        timestamptz started_at
        timestamptz completed_at
        text notes
    }

    workout_sets {
        uuid id PK
        uuid session_id FK
        uuid exercise_id FK
        int set_number
        int reps
        decimal weight_kg
        timestamptz completed_at
    }

    nutrition_logs {
        uuid id PK
        uuid user_id FK
        date logged_at
        varchar meal_name
        decimal calories
        decimal protein_g
        decimal carbs_g
        decimal fat_g
        text notes
    }

    goals {
        uuid id PK
        uuid user_id FK
        goal_type_enum goal_type
        decimal target_value
        varchar unit
        date target_date
        timestamptz created_at
        timestamptz updated_at
    }

    notification_preferences {
        uuid id PK
        uuid user_id FK_UK
        varchar fcm_token
        boolean reminders_enabled
        time reminder_time
        timestamptz updated_at
    }

    users ||--o{ workout_sessions : "has"
    users ||--o{ nutrition_logs : "logs"
    users ||--o{ goals : "sets"
    users ||--o| notification_preferences : "has"
    users ||--o{ exercises : "creates"
    muscle_groups ||--o{ exercises : "categorises"
    workout_sessions ||--o{ workout_sets : "contains"
    exercises ||--o{ workout_sets : "used in"
```

---

## Table Definitions

### `users`

Stores one record per Firebase authenticated user. Created on first login via `POST /api/v1/auth/register`.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, DEFAULT gen_random_uuid() | Internal ID used for all FK references |
| `firebase_uid` | `VARCHAR(128)` | UNIQUE NOT NULL | Firebase Auth UID — used to look up user from JWT claims |
| `email` | `VARCHAR(320)` | NOT NULL | Sourced from Firebase token claims |
| `display_name` | `VARCHAR(255)` | NULL | User-editable; initially from Firebase profile |
| `created_at` | `TIMESTAMPTZ` | NOT NULL DEFAULT now() | |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL DEFAULT now() | Updated by application on every PUT |

---

### `muscle_groups`

Reference table for categorising exercises. Seeded at migration time; user-extensible in future.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, DEFAULT gen_random_uuid() | |
| `name` | `VARCHAR(100)` | UNIQUE NOT NULL | e.g. "Chest", "Back", "Quadriceps" |
| `description` | `TEXT` | NULL | Optional human-readable description |

---

### `exercises`

Exercise library. System exercises have `created_by_user_id = NULL`; user-created exercises are owned.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, DEFAULT gen_random_uuid() | |
| `name` | `VARCHAR(255)` | NOT NULL | e.g. "Bench Press", "Squat" |
| `description` | `TEXT` | NULL | |
| `muscle_group_id` | `UUID` | NOT NULL, FK → muscle_groups(id) | Cascade: RESTRICT on delete |
| `instructions` | `TEXT` | NULL | Step-by-step how-to |
| `created_by_user_id` | `UUID` | NULL, FK → users(id) | NULL = system exercise; SET NULL on user delete |
| `created_at` | `TIMESTAMPTZ` | NOT NULL DEFAULT now() | |

---

### `workout_sessions`

A single workout session (e.g. "Leg Day — Monday"). Sets are recorded inside the session.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, DEFAULT gen_random_uuid() | |
| `user_id` | `UUID` | NOT NULL, FK → users(id) | CASCADE DELETE |
| `name` | `VARCHAR(255)` | NULL | Optional session label |
| `started_at` | `TIMESTAMPTZ` | NOT NULL DEFAULT now() | Session start timestamp |
| `completed_at` | `TIMESTAMPTZ` | NULL | NULL = session still in progress |
| `notes` | `TEXT` | NULL | Free-form post-session notes |

---

### `workout_sets`

Individual sets performed within a workout session.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, DEFAULT gen_random_uuid() | |
| `session_id` | `UUID` | NOT NULL, FK → workout_sessions(id) | CASCADE DELETE |
| `exercise_id` | `UUID` | NOT NULL, FK → exercises(id) | RESTRICT on exercise delete |
| `set_number` | `INT` | NOT NULL, CHECK (set_number > 0) | Ordering within the session for this exercise |
| `reps` | `INT` | NOT NULL, CHECK (reps > 0) | |
| `weight_kg` | `DECIMAL(6,2)` | NOT NULL, CHECK (weight_kg >= 0) | 0 = bodyweight; supports up to 9999.99 kg |
| `completed_at` | `TIMESTAMPTZ` | NOT NULL DEFAULT now() | When this set was logged |

---

### `nutrition_logs`

One row per meal entry per day. Multiple rows allowed per `(user_id, logged_at)` for different meals.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, DEFAULT gen_random_uuid() | |
| `user_id` | `UUID` | NOT NULL, FK → users(id) | CASCADE DELETE |
| `logged_at` | `DATE` | NOT NULL | Date of consumption (not timestamp) |
| `meal_name` | `VARCHAR(255)` | NOT NULL | e.g. "Breakfast", "Post-workout shake" |
| `calories` | `DECIMAL(8,2)` | NOT NULL, CHECK (calories >= 0) | kcal |
| `protein_g` | `DECIMAL(8,2)` | NOT NULL, CHECK (protein_g >= 0) | grams |
| `carbs_g` | `DECIMAL(8,2)` | NOT NULL, CHECK (carbs_g >= 0) | grams |
| `fat_g` | `DECIMAL(8,2)` | NOT NULL, CHECK (fat_g >= 0) | grams |
| `notes` | `TEXT` | NULL | Optional notes |

---

### `goals`

Fitness goals for a user. One user can have multiple goals of different types.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, DEFAULT gen_random_uuid() | |
| `user_id` | `UUID` | NOT NULL, FK → users(id) | CASCADE DELETE |
| `goal_type` | `goal_type` (ENUM) | NOT NULL | `TARGET_WEIGHT` or `WORKOUT_FREQUENCY` |
| `target_value` | `DECIMAL(10,2)` | NOT NULL | e.g. 75.00 (kg) or 4 (days/week) |
| `unit` | `VARCHAR(50)` | NOT NULL | e.g. "kg", "days_per_week" |
| `target_date` | `DATE` | NULL | Deadline; NULL = open-ended |
| `created_at` | `TIMESTAMPTZ` | NOT NULL DEFAULT now() | |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL DEFAULT now() | |

---

### `notification_preferences`

One row per user (1:1). UPSERT on every `POST /api/v1/notifications/token`.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `UUID` | PK, DEFAULT gen_random_uuid() | |
| `user_id` | `UUID` | UNIQUE NOT NULL, FK → users(id) | CASCADE DELETE; UNIQUE enforces 1:1 |
| `fcm_token` | `VARCHAR(512)` | NOT NULL | Firebase Cloud Messaging registration token |
| `reminders_enabled` | `BOOLEAN` | NOT NULL DEFAULT false | |
| `reminder_time` | `TIME` | NOT NULL DEFAULT '08:00' | Local time for daily reminder cron |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL DEFAULT now() | |

---

## Indexes

| Table | Index Name | Columns | Type | Rationale |
|-------|------------|---------|------|-----------|
| `users` | `users_firebase_uid_idx` | `firebase_uid` | UNIQUE (implicit) | JWT claims lookup on every request |
| `users` | `users_email_idx` | `email` | BTREE | Profile search |
| `exercises` | `exercises_muscle_group_id_idx` | `muscle_group_id` | BTREE | FK join; filter by muscle group |
| `exercises` | `exercises_created_by_user_id_idx` | `created_by_user_id` | BTREE | Lookup user's custom exercises |
| `exercises` | `exercises_name_search_idx` | `LOWER(name)` | BTREE | Case-insensitive name search |
| `workout_sessions` | `workout_sessions_user_id_idx` | `user_id` | BTREE | All sessions for a user |
| `workout_sessions` | `workout_sessions_user_started_idx` | `(user_id, started_at DESC)` | BTREE | Date-range paginated session list |
| `workout_sets` | `workout_sets_session_id_idx` | `session_id` | BTREE | All sets for a session |
| `workout_sets` | `workout_sets_exercise_id_idx` | `exercise_id` | BTREE | Metrics aggregation by exercise |
| `nutrition_logs` | `nutrition_logs_user_id_idx` | `user_id` | BTREE | All logs for a user |
| `nutrition_logs` | `nutrition_logs_user_date_idx` | `(user_id, logged_at DESC)` | BTREE | Date-range nutrition queries |
| `goals` | `goals_user_id_idx` | `user_id` | BTREE | All goals for a user |
| `notification_preferences` | `notification_preferences_user_id_idx` | `user_id` | UNIQUE (implicit) | 1:1 lookup |

---

## Migration SQL

### V1__create_schema.sql

```sql
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

    CONSTRAINT users_pk          PRIMARY KEY (id),
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

    CONSTRAINT muscle_groups_pk   PRIMARY KEY (id),
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

    CONSTRAINT exercises_pk                PRIMARY KEY (id),
    CONSTRAINT exercises_muscle_group_fk   FOREIGN KEY (muscle_group_id)
        REFERENCES muscle_groups (id) ON DELETE RESTRICT,
    CONSTRAINT exercises_created_by_fk     FOREIGN KEY (created_by_user_id)
        REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX exercises_muscle_group_id_idx  ON exercises (muscle_group_id);
CREATE INDEX exercises_created_by_user_id_idx ON exercises (created_by_user_id);
CREATE INDEX exercises_name_search_idx      ON exercises (LOWER(name));

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

CREATE INDEX workout_sessions_user_id_idx     ON workout_sessions (user_id);
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

    CONSTRAINT workout_sets_pk          PRIMARY KEY (id),
    CONSTRAINT workout_sets_session_fk  FOREIGN KEY (session_id)
        REFERENCES workout_sessions (id) ON DELETE CASCADE,
    CONSTRAINT workout_sets_exercise_fk FOREIGN KEY (exercise_id)
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

    CONSTRAINT nutrition_logs_pk      PRIMARY KEY (id),
    CONSTRAINT nutrition_logs_user_fk FOREIGN KEY (user_id)
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
```

### V1__create_schema_rollback.sql

```sql
-- ============================================================
-- V1__create_schema_rollback.sql  (Flyway undo migration)
-- ============================================================

DROP TABLE IF EXISTS notification_preferences;
DROP TABLE IF EXISTS goals;
DROP TABLE IF EXISTS nutrition_logs;
DROP TABLE IF EXISTS workout_sets;
DROP TABLE IF EXISTS workout_sessions;
DROP TABLE IF EXISTS exercises;
DROP TABLE IF EXISTS muscle_groups;
DROP TABLE IF EXISTS users;

DROP TYPE IF EXISTS goal_type;
```

---

## Seed Data SQL

### V2__seed_muscle_groups_and_exercises.sql

```sql
-- ============================================================
-- V2__seed_muscle_groups_and_exercises.sql
-- Flyway migration — seed system muscle groups and exercises
-- ============================================================

-- -------------------------------------------------------
-- Muscle groups
-- -------------------------------------------------------

INSERT INTO muscle_groups (id, name, description) VALUES
    ('11111111-0000-0000-0000-000000000001', 'Chest',
     'Pectoral muscles — upper, middle, and lower chest'),
    ('11111111-0000-0000-0000-000000000002', 'Back',
     'Latissimus dorsi, rhomboids, and trapezius'),
    ('11111111-0000-0000-0000-000000000003', 'Shoulders',
     'Anterior, lateral, and posterior deltoids'),
    ('11111111-0000-0000-0000-000000000004', 'Biceps',
     'Biceps brachii and brachialis'),
    ('11111111-0000-0000-0000-000000000005', 'Triceps',
     'Triceps brachii — long, lateral, and medial heads'),
    ('11111111-0000-0000-0000-000000000006', 'Quadriceps',
     'Rectus femoris, vastus lateralis, medialis, and intermedius'),
    ('11111111-0000-0000-0000-000000000007', 'Hamstrings',
     'Biceps femoris, semitendinosus, and semimembranosus'),
    ('11111111-0000-0000-0000-000000000008', 'Glutes',
     'Gluteus maximus, medius, and minimus'),
    ('11111111-0000-0000-0000-000000000009', 'Core',
     'Rectus abdominis, obliques, and transverse abdominis'),
    ('11111111-0000-0000-0000-000000000010', 'Cardio',
     'Cardiovascular and full-body conditioning exercises');

-- -------------------------------------------------------
-- System exercises (created_by_user_id = NULL)
-- -------------------------------------------------------

INSERT INTO exercises (id, name, description, muscle_group_id, instructions) VALUES

    -- Chest
    ('22222222-0000-0000-0000-000000000001', 'Barbell Bench Press',
     'Classic compound chest exercise using a barbell.',
     '11111111-0000-0000-0000-000000000001',
     '1. Lie flat on a bench.\n2. Grip the barbell slightly wider than shoulder-width.\n3. Lower the bar to mid-chest.\n4. Press back to full extension.'),

    ('22222222-0000-0000-0000-000000000002', 'Dumbbell Bench Press',
     'Dumbbell variation for greater range of motion.',
     '11111111-0000-0000-0000-000000000001',
     '1. Lie flat on a bench with a dumbbell in each hand.\n2. Lower dumbbells to chest level.\n3. Press upward until arms are extended.'),

    ('22222222-0000-0000-0000-000000000003', 'Push-Up',
     'Bodyweight chest exercise requiring no equipment.',
     '11111111-0000-0000-0000-000000000001',
     '1. Start in a high plank position.\n2. Lower your chest to the floor.\n3. Push back up to starting position.'),

    ('22222222-0000-0000-0000-000000000004', 'Incline Dumbbell Press',
     'Upper chest focused dumbbell press on an incline bench.',
     '11111111-0000-0000-0000-000000000001',
     '1. Set bench to 30–45° incline.\n2. Press dumbbells from shoulder level upward.\n3. Lower under control.'),

    -- Back
    ('22222222-0000-0000-0000-000000000005', 'Pull-Up',
     'Bodyweight vertical pulling movement for the back.',
     '11111111-0000-0000-0000-000000000002',
     '1. Hang from a bar with overhand grip.\n2. Pull until chin clears the bar.\n3. Lower back to dead hang.'),

    ('22222222-0000-0000-0000-000000000006', 'Barbell Deadlift',
     'Fundamental compound hinge movement targeting the posterior chain.',
     '11111111-0000-0000-0000-000000000002',
     '1. Stand with bar over mid-foot.\n2. Hinge at hips, grip the bar.\n3. Drive through the floor, extending hips and knees.\n4. Lower bar under control.'),

    ('22222222-0000-0000-0000-000000000007', 'Seated Cable Row',
     'Horizontal pulling exercise targeting mid-back.',
     '11111111-0000-0000-0000-000000000002',
     '1. Sit at cable row machine, feet on platform.\n2. Pull handle to lower abdomen.\n3. Squeeze shoulder blades together.\n4. Return to start.'),

    ('22222222-0000-0000-0000-000000000008', 'Lat Pulldown',
     'Vertical pulling movement to develop lat width.',
     '11111111-0000-0000-0000-000000000002',
     '1. Grip the bar wider than shoulder-width.\n2. Pull the bar to upper chest.\n3. Slowly return to the start position.'),

    -- Shoulders
    ('22222222-0000-0000-0000-000000000009', 'Overhead Press',
     'Compound shoulder press with a barbell.',
     '11111111-0000-0000-0000-000000000003',
     '1. Hold barbell at shoulder height.\n2. Press directly overhead.\n3. Lower under control.'),

    ('22222222-0000-0000-0000-000000000010', 'Dumbbell Lateral Raise',
     'Isolation exercise for the lateral deltoid.',
     '11111111-0000-0000-0000-000000000003',
     '1. Stand with dumbbells at sides.\n2. Raise arms to shoulder height laterally.\n3. Lower slowly.'),

    -- Biceps
    ('22222222-0000-0000-0000-000000000011', 'Barbell Curl',
     'Classic barbell bicep curl.',
     '11111111-0000-0000-0000-000000000004',
     '1. Stand with barbell in a supinated grip.\n2. Curl the bar to shoulder height.\n3. Lower with control.'),

    ('22222222-0000-0000-0000-000000000012', 'Dumbbell Hammer Curl',
     'Neutral-grip curl targeting brachialis and biceps.',
     '11111111-0000-0000-0000-000000000004',
     '1. Hold dumbbells with a neutral (hammer) grip.\n2. Curl to shoulder height.\n3. Lower slowly.'),

    -- Triceps
    ('22222222-0000-0000-0000-000000000013', 'Tricep Dips',
     'Bodyweight tricep exercise on parallel bars or bench.',
     '11111111-0000-0000-0000-000000000005',
     '1. Support yourself on parallel bars or a bench.\n2. Lower until elbows are at 90°.\n3. Press back up.'),

    ('22222222-0000-0000-0000-000000000014', 'Tricep Pushdown',
     'Cable isolation exercise for the tricep.',
     '11111111-0000-0000-0000-000000000005',
     '1. Stand at cable machine with rope or bar attachment.\n2. Push downward until elbows are fully extended.\n3. Return slowly.'),

    -- Quadriceps
    ('22222222-0000-0000-0000-000000000015', 'Barbell Back Squat',
     'Foundational lower body compound movement.',
     '11111111-0000-0000-0000-000000000006',
     '1. Bar on upper traps, feet shoulder-width.\n2. Squat until thighs are parallel to the floor.\n3. Drive through heels to stand.'),

    ('22222222-0000-0000-0000-000000000016', 'Leg Press',
     'Machine-based quad-dominant lower body exercise.',
     '11111111-0000-0000-0000-000000000006',
     '1. Sit in leg press machine.\n2. Lower the platform until knees are at 90°.\n3. Press back to full extension.'),

    ('22222222-0000-0000-0000-000000000017', 'Lunges',
     'Unilateral lower body exercise targeting quads and glutes.',
     '11111111-0000-0000-0000-000000000006',
     '1. Step forward with one leg.\n2. Lower back knee toward the floor.\n3. Push off front foot to return.'),

    -- Hamstrings
    ('22222222-0000-0000-0000-000000000018', 'Romanian Deadlift',
     'Hip hinge movement targeting hamstrings.',
     '11111111-0000-0000-0000-000000000007',
     '1. Hold barbell with overhand grip.\n2. Hinge at hips, lowering bar along legs.\n3. Drive hips forward to return.'),

    ('22222222-0000-0000-0000-000000000019', 'Lying Leg Curl',
     'Isolation exercise for the hamstrings.',
     '11111111-0000-0000-0000-000000000007',
     '1. Lie face-down on leg curl machine.\n2. Curl heels toward glutes.\n3. Lower slowly.'),

    -- Glutes
    ('22222222-0000-0000-0000-000000000020', 'Hip Thrust',
     'Glute-dominant barbell hip extension.',
     '11111111-0000-0000-0000-000000000008',
     '1. Sit with upper back on a bench, barbell across hips.\n2. Drive hips upward until fully extended.\n3. Lower under control.'),

    ('22222222-0000-0000-0000-000000000021', 'Glute Bridge',
     'Bodyweight glute activation exercise.',
     '11111111-0000-0000-0000-000000000008',
     '1. Lie on back with knees bent.\n2. Drive hips upward, squeezing glutes at top.\n3. Lower slowly.'),

    -- Core
    ('22222222-0000-0000-0000-000000000022', 'Plank',
     'Isometric core stabilisation exercise.',
     '11111111-0000-0000-0000-000000000009',
     '1. Hold a forearm plank position.\n2. Keep hips level and core braced.\n3. Hold for target duration.'),

    ('22222222-0000-0000-0000-000000000023', 'Crunch',
     'Basic abdominal flexion exercise.',
     '11111111-0000-0000-0000-000000000009',
     '1. Lie on back with knees bent.\n2. Curl shoulders toward knees.\n3. Lower under control.'),

    ('22222222-0000-0000-0000-000000000024', 'Russian Twist',
     'Rotational core exercise targeting obliques.',
     '11111111-0000-0000-0000-000000000009',
     '1. Sit with knees bent, lean back slightly.\n2. Rotate torso left and right.\n3. Add weight for increased difficulty.'),

    -- Cardio
    ('22222222-0000-0000-0000-000000000025', 'Treadmill Run',
     'Cardiovascular running on a treadmill.',
     '11111111-0000-0000-0000-000000000010',
     '1. Set speed and incline.\n2. Run for target duration or distance.\n3. Cool down with a 5-minute walk.'),

    ('22222222-0000-0000-0000-000000000026', 'Rowing Machine',
     'Full-body cardio exercise on a rowing ergometer.',
     '11111111-0000-0000-0000-000000000010',
     '1. Sit on the seat, feet strapped in.\n2. Drive with legs, then lean back, then pull handle to chest.\n3. Reverse the sequence to return.'),

    ('22222222-0000-0000-0000-000000000027', 'Cycling',
     'Stationary bike cardiovascular exercise.',
     '11111111-0000-0000-0000-000000000010',
     '1. Adjust seat height.\n2. Pedal at target cadence and resistance.\n3. Cool down at lower resistance.');
```

### V2__seed_rollback.sql

```sql
-- ============================================================
-- V2__seed_rollback.sql  (Flyway undo migration)
-- ============================================================

DELETE FROM exercises WHERE id LIKE '22222222-0000-0000-0000-0000000000%';
DELETE FROM muscle_groups WHERE id LIKE '11111111-0000-0000-0000-0000000000%';
```

---

## Query Patterns Reference

### Session list for user (paginated, newest first)

```sql
SELECT id, name, started_at, completed_at, notes
FROM workout_sessions
WHERE user_id = $1
ORDER BY started_at DESC
LIMIT $2 OFFSET $3;
-- Uses: workout_sessions_user_started_idx
```

### Daily nutrition macro summary

```sql
SELECT
    logged_at,
    SUM(calories)  AS total_calories,
    SUM(protein_g) AS total_protein_g,
    SUM(carbs_g)   AS total_carbs_g,
    SUM(fat_g)     AS total_fat_g
FROM nutrition_logs
WHERE user_id  = $1
  AND logged_at BETWEEN $2 AND $3
GROUP BY logged_at
ORDER BY logged_at DESC;
-- Uses: nutrition_logs_user_date_idx
```

### Workout volume per exercise (metrics)

```sql
SELECT
    e.name                           AS exercise_name,
    COUNT(ws.id)                     AS total_sets,
    SUM(ws.reps)                     AS total_reps,
    SUM(ws.reps * ws.weight_kg)      AS total_volume_kg,
    MAX(ws.weight_kg)                AS max_weight_kg
FROM workout_sets ws
JOIN workout_sessions sess ON sess.id = ws.session_id
JOIN exercises e            ON e.id   = ws.exercise_id
WHERE sess.user_id = $1
  AND sess.started_at BETWEEN $2 AND $3
GROUP BY e.id, e.name
ORDER BY total_volume_kg DESC;
-- Uses: workout_sessions_user_started_idx, workout_sets_session_id_idx
```

### Exercise search by name

```sql
SELECT id, name, description, muscle_group_id
FROM exercises
WHERE LOWER(name) LIKE '%' || LOWER($1) || '%'
ORDER BY name
LIMIT 20;
-- Uses: exercises_name_search_idx
```
