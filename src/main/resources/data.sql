-- Users (Seeding Data)
-- 유저 정보만 초기화, 회의 데이터는 스크립트 업로드로 생성

INSERT INTO users (id, nickname, profile_image_url, arrc_type, character_badge_label, strength_tags, weakness_tags, created_at)
VALUES (1, '루시', '/lucy.png', 'TYPE_SUPPORTIVE', '소심형', '경청,공감,배려', '주장,결단', NOW());

INSERT INTO users (id, nickname, profile_image_url, arrc_type, character_badge_label, strength_tags, weakness_tags, created_at)
VALUES (2, '제피', '/zephy.jpg', 'TYPE_DRIVER', '자기중심적', '추진력,명확함,효율', '공감,배려,경청', NOW());

INSERT INTO users (id, nickname, profile_image_url, arrc_type, character_badge_label, strength_tags, weakness_tags, created_at)
VALUES (3, '제임스', '/james.jpg', 'TYPE_EXPRESSIVE', '절제된 리더', '리더십,유머,조율', '디테일,구체성', NOW());

-- Reset sequences to avoid ID conflicts
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE meetings ALTER COLUMN id RESTART WITH 1;
ALTER TABLE meeting_participants ALTER COLUMN id RESTART WITH 1;
ALTER TABLE meeting_relationships ALTER COLUMN id RESTART WITH 1;
ALTER TABLE chat_messages ALTER COLUMN id RESTART WITH 1;
