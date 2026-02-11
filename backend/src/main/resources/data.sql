-- Seed a default API user for JWT authentication.
-- Password encoding uses Spring's DelegatingPasswordEncoder with {noop} prefix.

-- Admin user
INSERT INTO users (id, username, password, enabled, full_name, email)
VALUES (1, 'admin', '{noop}admin', 1, 'System Administrator', 'admin@mts.com')
ON DUPLICATE KEY UPDATE username = VALUES(username),
                            password = VALUES(password),
                            enabled = VALUES(enabled),
                            full_name = VALUES(full_name),
                            email = VALUES(email);

-- Regular test user
INSERT INTO users (id, username, password, enabled, full_name, email)
VALUES (2, 'testuser', '{noop}password', 1, 'Test User', 'testuser@mts.com')
ON DUPLICATE KEY UPDATE username = VALUES(username),
                            password = VALUES(password),
                            enabled = VALUES(enabled),
                            full_name = VALUES(full_name),
                            email = VALUES(email);

-- Insert ADMIN role for the admin user
INSERT INTO user_roles (user_id, role)
VALUES (1, 'ADMIN')
ON DUPLICATE KEY UPDATE role = VALUES(role);

-- Insert USER role for the test user
INSERT INTO user_roles (user_id, role)
VALUES (2, 'USER')
ON DUPLICATE KEY UPDATE role = VALUES(role);