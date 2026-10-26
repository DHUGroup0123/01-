-- 01电商系统: run once using a MySQL account allowed to create a development database.
CREATE DATABASE IF NOT EXISTS mall_01 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Then configure DB_USERNAME / DB_PASSWORD and start the application with profile init.
-- Schema and data have a single source: src/main/resources/db/V001__*.sql.
