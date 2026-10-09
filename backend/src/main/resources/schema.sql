CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    firebase_uid VARCHAR(128) NOT NULL,
    email VARCHAR(320),
    display_name VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_firebase_uid ON users (firebase_uid);

CREATE TABLE IF NOT EXISTS health_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    calorie_target INTEGER CHECK (calorie_target > 0),
    household_size INTEGER NOT NULL DEFAULT 1 CHECK (household_size > 0),
    weekly_budget_bdt NUMERIC(12, 2) CHECK (weekly_budget_bdt >= 0),
    dietary_tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS meal_plans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    starts_on DATE NOT NULL,
    ends_on DATE NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    is_fallback BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (ends_on >= starts_on)
);

ALTER TABLE meal_plans ADD COLUMN IF NOT EXISTS is_fallback BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS recipes (
    id BIGSERIAL PRIMARY KEY,
    meal_plan_id BIGINT NOT NULL REFERENCES meal_plans (id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    meal_type VARCHAR(24) NOT NULL,
    name VARCHAR(240) NOT NULL,
    prep_minutes INTEGER CHECK (prep_minutes >= 0),
    estimated_cost_bdt NUMERIC(12, 2) CHECK (estimated_cost_bdt >= 0),
    ingredients JSONB NOT NULL DEFAULT '[]'::jsonb,
    instructions JSONB NOT NULL DEFAULT '[]'::jsonb
);

CREATE TABLE IF NOT EXISTS grocery_items (
    id BIGSERIAL PRIMARY KEY,
    meal_plan_id BIGINT NOT NULL REFERENCES meal_plans (id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(60) NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL CHECK (quantity >= 0),
    unit VARCHAR(40) NOT NULL,
    in_pantry BOOLEAN NOT NULL DEFAULT FALSE,
    checked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS logged_meals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    image_url TEXT,
    dish_name VARCHAR(240) NOT NULL,
    estimated_calories INTEGER CHECK (estimated_calories >= 0),
    protein_grams NUMERIC(8, 2) CHECK (protein_grams >= 0),
    carbs_grams NUMERIC(8, 2) CHECK (carbs_grams >= 0),
    fats_grams NUMERIC(8, 2) CHECK (fats_grams >= 0),
    logged_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);