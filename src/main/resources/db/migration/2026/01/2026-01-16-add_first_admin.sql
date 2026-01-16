INSERT INTO users (username, password, role)
VALUES (
    'admin',
    -- шифр для пароля "admin123"
    '$2a$10$t/xY5Y1FxviJt1J04VFosu7KTOM7QaEiSWLZZl5d.d/9US7LRtzEu',
    'ROLE_ADMIN'
   )
ON CONFLICT (username) DO NOTHING;