-- Create admin user
-- Note: Password 'pw12345&A' is encoded using BCrypt
INSERT INTO users (uuid, first_name, last_name, email, password)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Dzon', 'Don', 'dzon@email.com',
        '$2a$10$b.ZT.qRaCNo103sVUlnRfuzdIprEaZgDDr0xEGK/ez4Sjaha4Jbi6');

-- Add ADMIN role for the user
INSERT INTO user_roles (uuid, user_uuid, role)
VALUES ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'ADMIN');