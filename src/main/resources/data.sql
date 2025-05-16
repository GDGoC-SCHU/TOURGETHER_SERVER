-- src/main/resources/data.sql

-- MBTI 태그 삽입
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ENFP', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ENFJ', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ENTP', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ENTJ', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('INFJ', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('INFP', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('INTP', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('INTJ', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ESTP', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ESTJ', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ESFP', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ESFJ', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ISTP', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ISTJ', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ISFP', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('ISFJ', 'MBTI', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;

-- 관심사(INTEREST) 태그 삽입 - 확장된 목록
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Cafe', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Food', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('SightSeeing', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Activity', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Staycation', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Nature', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Beach', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Mountain', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('History', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Culture', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Shopping', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Nightlife', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Festival', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Adventure', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Relaxation', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('LocalFood', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Museum', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Architecture', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('IslandHopping', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('RoadTrip', 'INTEREST', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;

-- 취미(HOBBY) 태그 삽입 - 확장된 목록
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Music', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Movie', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Reading', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Sports', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Art', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Gaming', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Hiking', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Photography', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Cooking', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Traveling', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Dancing', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Singing', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Writing', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Painting', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Gardening', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Yoga', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Meditation', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Drawing', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Camping', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Cycling', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Swimming', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Crafting', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Baking', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Running', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Fishing', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Knitting', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Blogging', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Podcasting', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Volunteering', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, category, created_at, updated_at) VALUES ('Boardgames', 'HOBBY', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;