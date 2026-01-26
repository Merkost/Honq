# Supabase Database Schema

## Overview
Flexible schema designed to support driving knowledge tests for all Australian states/territories, starting with NSW.
Questions are organized by `question_sets` (state + license type), while categories remain global.

---

## Tables

### `states`
Australian states and territories. Per-license test rules live in `question_sets`.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `text` | Primary key (e.g., "nsw", "vic", "qld") |
| `name` | `text` | Display name (e.g., "New South Wales") |
| `short_name` | `text` | Abbreviation (e.g., "NSW") |
| `is_active` | `boolean` | Whether content is available |
| `mock_test_question_count` | `integer` | Deprecated: use `question_sets.mock_test_question_count` |
| `mock_test_time_limit_minutes` | `integer` | Deprecated: use `question_sets.mock_test_time_limit_minutes` |
| `mock_test_pass_percentage` | `integer` | Deprecated: use `question_sets.mock_test_pass_percentage` |
| `created_at` | `timestamptz` | Record creation time |
| `updated_at` | `timestamptz` | Last update time |

```sql
CREATE TABLE states (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    short_name TEXT NOT NULL,
    is_active BOOLEAN DEFAULT false,
    mock_test_question_count INTEGER DEFAULT 45,
    mock_test_time_limit_minutes INTEGER DEFAULT 45,
    mock_test_pass_percentage INTEGER DEFAULT 75,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO states (id, name, short_name, is_active) VALUES
('nsw', 'New South Wales', 'NSW', true),
('vic', 'Victoria', 'VIC', false),
('qld', 'Queensland', 'QLD', false),
('sa', 'South Australia', 'SA', false),
('wa', 'Western Australia', 'WA', false),
('tas', 'Tasmania', 'TAS', false),
('nt', 'Northern Territory', 'NT', false),
('act', 'Australian Capital Territory', 'ACT', false);
```

---

### `license_types`
License types supported by the question bank (car, rider, heavy rigid, etc).

| Column | Type | Description |
|--------|------|-------------|
| `id` | `text` | Primary key (e.g., "car", "rider") |
| `name` | `text` | Display name (e.g., "Car") |
| `short_name` | `text` | Abbreviation (e.g., "C") |
| `is_active` | `boolean` | Whether content is available |
| `display_order` | `integer` | Sort order |
| `created_at` | `timestamptz` | Record creation time |
| `updated_at` | `timestamptz` | Last update time |

```sql
CREATE TABLE license_types (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    short_name TEXT NOT NULL,
    is_active BOOLEAN DEFAULT true,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO license_types (id, name, short_name, display_order) VALUES
('car', 'Car', 'C', 1),
('rider', 'Rider', 'R', 2),
('heavy_rigid', 'Heavy Rigid', 'HR', 3);
```

---

### `question_sets`
Per-state, per-license question sets and test rules.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `text` | Primary key (e.g., "nsw_car") |
| `state_id` | `text` | Foreign key to states |
| `license_type_id` | `text` | Foreign key to license_types |
| `is_active` | `boolean` | Whether content is available |
| `mock_test_question_count` | `integer` | Questions per mock test (e.g., 45) |
| `mock_test_time_limit_minutes` | `integer` | Time limit (e.g., 45) |
| `mock_test_pass_percentage` | `integer` | Pass threshold (e.g., 75) |
| `created_at` | `timestamptz` | Record creation time |
| `updated_at` | `timestamptz` | Last update time |

```sql
CREATE TABLE question_sets (
    id TEXT PRIMARY KEY,
    state_id TEXT NOT NULL REFERENCES states(id),
    license_type_id TEXT NOT NULL REFERENCES license_types(id),
    is_active BOOLEAN DEFAULT true,
    mock_test_question_count INTEGER DEFAULT 45,
    mock_test_time_limit_minutes INTEGER DEFAULT 45,
    mock_test_pass_percentage INTEGER DEFAULT 75,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (state_id, license_type_id)
);

CREATE INDEX idx_question_sets_state ON question_sets(state_id);
CREATE INDEX idx_question_sets_license_type ON question_sets(license_type_id);
CREATE INDEX idx_question_sets_active ON question_sets(is_active);

INSERT INTO question_sets (id, state_id, license_type_id, is_active) VALUES
('nsw_car', 'nsw', 'car', true);
```

---

### `categories`
Question categories/topics.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `text` | Primary key (e.g., "road_rules") |
| `name` | `text` | Display name (e.g., "Road Rules") |
| `description` | `text` | Category description |
| `icon_name` | `text` | Icon identifier |
| `display_order` | `integer` | Sort order |
| `is_active` | `boolean` | Whether category is active |
| `created_at` | `timestamptz` | Record creation time |
| `updated_at` | `timestamptz` | Last update time |

```sql
CREATE TABLE categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    icon_name TEXT,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO categories (id, name, description, display_order) VALUES
('ROAD_RULES', 'Road Rules', 'General road rules and regulations', 1),
('ROAD_SIGNS', 'Road Signs', 'Traffic signs and their meanings', 2),
('SAFETY', 'Safety', 'Safe driving practices', 3),
('ALCOHOL_DRUGS', 'Alcohol & Drugs', 'Drink driving and drug laws', 4),
('HAZARDS', 'Hazard Perception', 'Identifying and responding to hazards', 5),
('PASSENGERS', 'Passengers & Load', 'Rules for carrying passengers and loads', 6),
('INTERSECTIONS', 'Intersections', 'Intersection rules and right of way', 7),
('SPEED_LIMITS', 'Speed Limits', 'Speed zones and limits', 8);
```

Categories are global. To list categories for a specific question set, query the
distinct categories used by questions in that set. If you need per-set ordering
or categories that can be empty, create `question_set_categories`:

```sql
CREATE TABLE question_set_categories (
    question_set_id TEXT NOT NULL REFERENCES question_sets(id) ON DELETE CASCADE,
    category_id TEXT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    display_order INTEGER DEFAULT 0,
    PRIMARY KEY (question_set_id, category_id)
);

CREATE INDEX idx_question_set_categories_set ON question_set_categories(question_set_id);
```

---

### `questions`
The main questions table.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `text` | Primary key (e.g., "nsw-001") |
| `question_set_id` | `text` | Foreign key to question_sets |
| `state_id` | `text` | Deprecated: keep temporarily for backfill |
| `category` | `text` | Category name (e.g., "ROAD_RULES") |
| `text` | `text` | Question text |
| `options` | `jsonb` | Array of answer options |
| `correct_index` | `integer` | Index of correct answer (0-based) |
| `explanation` | `text` | Explanation of correct answer |
| `image_url` | `text` | Optional image URL |
| `difficulty` | `integer` | 1=Easy, 2=Medium, 3=Hard |
| `is_active` | `boolean` | Whether question is active |
| `version` | `integer` | For tracking updates |
| `source` | `text` | Where question came from |
| `created_at` | `timestamptz` | Record creation time |
| `updated_at` | `timestamptz` | Last update time |

```sql
CREATE TABLE questions (
    id TEXT PRIMARY KEY,
    question_set_id TEXT NOT NULL REFERENCES question_sets(id),
    state_id TEXT NOT NULL REFERENCES states(id) DEFAULT 'nsw',
    category TEXT NOT NULL,
    text TEXT NOT NULL,
    options JSONB NOT NULL,
    correct_index INTEGER NOT NULL,
    explanation TEXT NOT NULL,
    image_url TEXT,
    difficulty INTEGER DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    is_active BOOLEAN DEFAULT true,
    version INTEGER DEFAULT 1,
    source TEXT DEFAULT 'manual',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_questions_question_set_active ON questions(question_set_id, is_active);
CREATE INDEX idx_questions_question_set_category ON questions(question_set_id, category);
CREATE INDEX idx_questions_state ON questions(state_id);
CREATE INDEX idx_questions_category ON questions(category);
CREATE INDEX idx_questions_state_active ON questions(state_id, is_active);
CREATE INDEX idx_questions_updated ON questions(updated_at);
```

---

### `question_images`
Storage for question images (metadata only, actual files in Supabase Storage).

| Column | Type | Description |
|--------|------|-------------|
| `id` | `uuid` | Primary key |
| `question_id` | `uuid` | Foreign key to questions |
| `storage_path` | `text` | Path in Supabase Storage |
| `alt_text` | `text` | Accessibility description |
| `created_at` | `timestamptz` | Record creation time |

```sql
CREATE TABLE question_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    storage_path TEXT NOT NULL,
    alt_text TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

### `app_config`
App-wide configuration.

| Column | Type | Description |
|--------|------|-------------|
| `key` | `text` | Config key |
| `value` | `jsonb` | Config value |
| `updated_at` | `timestamptz` | Last update time |

```sql
CREATE TABLE app_config (
    key TEXT PRIMARY KEY,
    value JSONB NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO app_config (key, value) VALUES
('min_app_version', '"1.0.0"'),
('maintenance_mode', 'false'),
('featured_state', '"nsw"');
```

---

## Row Level Security (RLS)

```sql
-- Enable RLS
ALTER TABLE states ENABLE ROW LEVEL SECURITY;
ALTER TABLE license_types ENABLE ROW LEVEL SECURITY;
ALTER TABLE question_sets ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE question_set_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE questions ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;

-- Public read access (no auth required for reading)
CREATE POLICY "Public read access" ON states FOR SELECT USING (true);
CREATE POLICY "Public read access" ON license_types FOR SELECT USING (is_active = true);
CREATE POLICY "Public read access" ON question_sets FOR SELECT USING (is_active = true);
CREATE POLICY "Public read access" ON categories FOR SELECT USING (true);
CREATE POLICY "Public read access" ON question_set_categories FOR SELECT USING (true);
CREATE POLICY "Public read access" ON questions FOR SELECT USING (is_active = true);
CREATE POLICY "Public read access" ON app_config FOR SELECT USING (true);
```

---

## Auto-Update Triggers

```sql
-- Function to auto-update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers
CREATE TRIGGER update_questions_updated_at
    BEFORE UPDATE ON questions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_states_updated_at
    BEFORE UPDATE ON states
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_license_types_updated_at
    BEFORE UPDATE ON license_types
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_question_sets_updated_at
    BEFORE UPDATE ON question_sets
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

## Views

### `active_questions_with_details`
Convenient view joining questions with state and category info.

```sql
CREATE VIEW active_questions_with_details AS
SELECT
    q.id,
    q.text,
    q.options,
    q.correct_index,
    q.explanation,
    q.image_url,
    q.difficulty,
    q.question_set_id,
    qs.state_id,
    s.name as state_name,
    qs.license_type_id,
    lt.name as license_type_name,
    q.category,
    q.updated_at
FROM questions q
JOIN question_sets qs ON q.question_set_id = qs.id
JOIN states s ON qs.state_id = s.id
JOIN license_types lt ON qs.license_type_id = lt.id
WHERE q.is_active = true
  AND qs.is_active = true
  AND s.is_active = true
  AND lt.is_active = true;
```

---

## Functions

### Get random questions for practice

```sql
CREATE OR REPLACE FUNCTION get_random_questions(
    p_question_set_id TEXT,
    p_count INTEGER DEFAULT 10,
    p_category TEXT DEFAULT NULL
)
RETURNS SETOF questions AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM questions
    WHERE question_set_id = p_question_set_id
      AND is_active = true
      AND (p_category IS NULL OR category = p_category)
    ORDER BY RANDOM()
    LIMIT p_count;
END;
$$ LANGUAGE plpgsql;
```

### Get mock test questions

```sql
CREATE OR REPLACE FUNCTION get_mock_test_questions(p_question_set_id TEXT)
RETURNS SETOF questions AS $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT mock_test_question_count INTO v_count
    FROM question_sets WHERE id = p_question_set_id;

    RETURN QUERY
    SELECT *
    FROM questions
    WHERE question_set_id = p_question_set_id AND is_active = true
    ORDER BY RANDOM()
    LIMIT v_count;
END;
$$ LANGUAGE plpgsql;
```

---

## Sample Data (NSW)

```sql
INSERT INTO license_types (id, name, short_name, display_order) VALUES
('car', 'Car', 'C', 1);

INSERT INTO question_sets (id, state_id, license_type_id, is_active) VALUES
('nsw_car', 'nsw', 'car', true);

INSERT INTO questions (id, question_set_id, state_id, category, text, options, correct_index, explanation, difficulty) VALUES
('nsw-001', 'nsw_car', 'nsw', 'INTERSECTIONS',
 'When approaching a roundabout, you must:',
 '["Speed up to enter quickly", "Give way to vehicles already in the roundabout", "Always stop before entering", "Sound your horn to warn other drivers"]',
 1,
 'You must give way to all vehicles already in the roundabout before entering.',
 2),

('nsw-002', 'nsw_car', 'nsw', 'SPEED_LIMITS',
 'What is the maximum speed limit in a school zone during school hours?',
 '["50 km/h", "40 km/h", "60 km/h", "30 km/h"]',
 1,
 'The speed limit in school zones is 40 km/h during school zone hours.',
 1),

('nsw-003', 'nsw_car', 'nsw', 'ROAD_RULES',
 'When can you use a mobile phone while driving?',
 '["When stopped at traffic lights", "Never while the vehicle is moving or stationary but not parked", "When driving under 40 km/h", "When using hands-free only"]',
 1,
 'You cannot use a hand-held mobile phone while driving, even when stopped at traffic lights.',
 2),

('nsw-004', 'nsw_car', 'nsw', 'ALCOHOL_DRUGS',
 'What is the blood alcohol limit for learner and P1 drivers?',
 '["0.02", "0.05", "Zero (0.00)", "0.01"]',
 2,
 'Learner and P1 provisional drivers must have a zero blood alcohol concentration.',
 1),

('nsw-005', 'nsw_car', 'nsw', 'SAFETY',
 'What should you do when an emergency vehicle approaches with flashing lights?',
 '["Speed up to get out of the way", "Stop immediately wherever you are", "Move left and stop if safe", "Continue driving normally"]',
 2,
 'You must move to the left side of the road and stop if safe to allow emergency vehicles to pass.',
 2);
```

---

## Storage Buckets

```sql
-- Create storage bucket for question images
INSERT INTO storage.buckets (id, name, public)
VALUES ('question-images', 'question-images', true);

-- Allow public read access
CREATE POLICY "Public read access"
ON storage.objects FOR SELECT
USING (bucket_id = 'question-images');
```

---

## Sync Strategy

### Client-side sync logic:
1. Store `last_sync_timestamp` locally per question set
2. Fetch questions where `updated_at > last_sync_timestamp`
3. Upsert into local Room database
4. Update `last_sync_timestamp`

### Endpoint for delta sync:
```sql
CREATE OR REPLACE FUNCTION get_updated_questions(
    p_question_set_id TEXT,
    p_since TIMESTAMPTZ
)
RETURNS SETOF questions AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM questions
    WHERE question_set_id = p_question_set_id
      AND updated_at > p_since;
END;
$$ LANGUAGE plpgsql;
```

---

## App Configuration

After setting up your Supabase project and running the schema, configure the app:

### 1. Get your credentials
- Go to Supabase Dashboard > Settings > API
- Copy the **Project URL** and **anon/public key**

### 2. Configure the app
In your app initialization (e.g., `Application.onCreate()` on Android or app entry point on iOS):

```kotlin
import com.merkost.honq.data.remote.api.SupabaseConfig

// Configure before any repository access
SupabaseConfig.configure(
    url = "https://YOUR_PROJECT_ID.supabase.co",
    key = "YOUR_ANON_KEY"
)
```

### 3. Test the connection
The app will automatically:
- Use `QuestionRepositoryImpl` when Supabase is configured
- Fall back to `FakeQuestionRepository` when not configured
- Perform delta sync using `updated_at` timestamps

---

## Future Considerations

1. **User Accounts** - Track progress across devices
2. **Leaderboards** - Compare scores with others
3. **Question Reporting** - Let users flag incorrect questions
4. **A/B Testing** - Test different question phrasings
5. **Analytics** - Track which questions are hardest
