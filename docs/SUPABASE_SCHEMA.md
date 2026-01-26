# Supabase Database Schema

## Overview
Flexible schema designed to support driving knowledge tests for all Australian states/territories, starting with NSW.

---

## Tables

### `states`
Australian states and territories with their test configurations.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `text` | Primary key (e.g., "nsw", "vic", "qld") |
| `name` | `text` | Display name (e.g., "New South Wales") |
| `short_name` | `text` | Abbreviation (e.g., "NSW") |
| `is_active` | `boolean` | Whether content is available |
| `mock_test_question_count` | `integer` | Questions per mock test (e.g., 45) |
| `mock_test_time_limit_minutes` | `integer` | Time limit (e.g., 45) |
| `mock_test_pass_percentage` | `integer` | Pass threshold (e.g., 75) |
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

### `categories`
Question categories/topics.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `text` | Primary key (e.g., "road_rules") |
| `name` | `text` | Display name (e.g., "Road Rules") |
| `description` | `text` | Category description |
| `icon` | `text` | Icon identifier |
| `display_order` | `integer` | Sort order |
| `created_at` | `timestamptz` | Record creation time |

```sql
CREATE TABLE categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    icon TEXT,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO categories (id, name, description, display_order) VALUES
('road_rules', 'Road Rules', 'General road rules and regulations', 1),
('road_signs', 'Road Signs', 'Traffic signs and their meanings', 2),
('safety', 'Safety', 'Safe driving practices', 3),
('alcohol_drugs', 'Alcohol & Drugs', 'Drink driving and drug laws', 4),
('hazards', 'Hazard Perception', 'Identifying and responding to hazards', 5),
('passengers', 'Passengers & Load', 'Rules for carrying passengers and loads', 6),
('intersections', 'Intersections', 'Intersection rules and right of way', 7),
('speed_limits', 'Speed Limits', 'Speed zones and limits', 8);
```

---

### `questions`
The main questions table.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `uuid` | Primary key |
| `state_id` | `text` | Foreign key to states |
| `category_id` | `text` | Foreign key to categories |
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
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    state_id TEXT NOT NULL REFERENCES states(id),
    category_id TEXT NOT NULL REFERENCES categories(id),
    text TEXT NOT NULL,
    options JSONB NOT NULL,
    correct_index INTEGER NOT NULL,
    explanation TEXT NOT NULL,
    image_url TEXT,
    difficulty INTEGER DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    is_active BOOLEAN DEFAULT true,
    version INTEGER DEFAULT 1,
    source TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_questions_state ON questions(state_id);
CREATE INDEX idx_questions_category ON questions(category_id);
CREATE INDEX idx_questions_active ON questions(is_active) WHERE is_active = true;
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
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE questions ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;

-- Public read access (no auth required for reading)
CREATE POLICY "Public read access" ON states FOR SELECT USING (true);
CREATE POLICY "Public read access" ON categories FOR SELECT USING (true);
CREATE POLICY "Public read access" ON questions FOR SELECT USING (is_active = true);
CREATE POLICY "Public read access" ON app_config FOR SELECT USING (true);
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
    q.state_id,
    s.name as state_name,
    q.category_id,
    c.name as category_name,
    q.updated_at
FROM questions q
JOIN states s ON q.state_id = s.id
JOIN categories c ON q.category_id = c.id
WHERE q.is_active = true AND s.is_active = true;
```

---

## Functions

### Get random questions for practice

```sql
CREATE OR REPLACE FUNCTION get_random_questions(
    p_state_id TEXT,
    p_count INTEGER DEFAULT 10,
    p_category_id TEXT DEFAULT NULL
)
RETURNS SETOF questions AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM questions
    WHERE state_id = p_state_id
      AND is_active = true
      AND (p_category_id IS NULL OR category_id = p_category_id)
    ORDER BY RANDOM()
    LIMIT p_count;
END;
$$ LANGUAGE plpgsql;
```

### Get mock test questions

```sql
CREATE OR REPLACE FUNCTION get_mock_test_questions(p_state_id TEXT)
RETURNS SETOF questions AS $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT mock_test_question_count INTO v_count
    FROM states WHERE id = p_state_id;

    RETURN QUERY
    SELECT *
    FROM questions
    WHERE state_id = p_state_id AND is_active = true
    ORDER BY RANDOM()
    LIMIT v_count;
END;
$$ LANGUAGE plpgsql;
```

---

## Sample Data (NSW)

```sql
INSERT INTO questions (state_id, category_id, text, options, correct_index, explanation) VALUES
('nsw', 'road_rules',
 'When approaching a roundabout, you must:',
 '["Speed up to enter quickly", "Give way to vehicles already in the roundabout", "Always stop before entering", "Sound your horn to warn other drivers"]',
 1,
 'You must give way to all vehicles already in the roundabout before entering.'),

('nsw', 'road_signs',
 'What is the maximum speed limit in a school zone during school hours?',
 '["50 km/h", "40 km/h", "60 km/h", "30 km/h"]',
 1,
 'The speed limit in school zones is 40 km/h during school zone hours.'),

('nsw', 'road_rules',
 'When can you use a mobile phone while driving?',
 '["When stopped at traffic lights", "Never while the vehicle is moving or stationary but not parked", "When driving under 40 km/h", "When using hands-free only"]',
 1,
 'You cannot use a hand-held mobile phone while driving, even when stopped at traffic lights.');
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
1. Store `last_sync_timestamp` locally
2. Fetch questions where `updated_at > last_sync_timestamp`
3. Upsert into local Room database
4. Update `last_sync_timestamp`

### Endpoint for delta sync:
```sql
CREATE OR REPLACE FUNCTION get_updated_questions(
    p_state_id TEXT,
    p_since TIMESTAMPTZ
)
RETURNS SETOF questions AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM questions
    WHERE state_id = p_state_id
      AND updated_at > p_since;
END;
$$ LANGUAGE plpgsql;
```

---

## Future Considerations

1. **User Accounts** - Track progress across devices
2. **Leaderboards** - Compare scores with others
3. **Question Reporting** - Let users flag incorrect questions
4. **A/B Testing** - Test different question phrasings
5. **Analytics** - Track which questions are hardest
