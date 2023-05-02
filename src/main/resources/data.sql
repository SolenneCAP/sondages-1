INSERT INTO users (username, password, enabled)
VALUES ('admin', '{bcrypt}$2a$10$fKQ/Ti4P.FyPfR7OgDU08OGYP6Ug9sFwLQ5RQBSktAlqqegnzGWra', 1);
INSERT INTO authorities (username, authority)
VALUES ('admin', 'ROLE_ADMIN');