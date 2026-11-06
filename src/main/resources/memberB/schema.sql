-- Member B schema for a NEW development database. Never drops tables or runs automatically.
CREATE TABLE IF NOT EXISTS tb_newbee_mall_admin_user (
 admin_user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
 login_user_name VARCHAR(50) NOT NULL UNIQUE, login_password VARCHAR(255) NOT NULL,
 nick_name VARCHAR(50) NOT NULL, locked INT NOT NULL DEFAULT 0,
 credential_version INT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS tb_newbee_mall_goods_category (
 category_id BIGINT PRIMARY KEY AUTO_INCREMENT, category_level INT NOT NULL,
 parent_id BIGINT NOT NULL DEFAULT 0, category_name VARCHAR(50) NOT NULL,
 category_rank INT NOT NULL DEFAULT 0, is_deleted INT NOT NULL DEFAULT 0,
 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS tb_newbee_mall_goods_info (
 goods_id BIGINT PRIMARY KEY AUTO_INCREMENT, goods_name VARCHAR(200) NOT NULL,
 goods_intro VARCHAR(200) NOT NULL DEFAULT '', goods_category_id BIGINT NOT NULL,
 goods_cover_img VARCHAR(200) NOT NULL, goods_carousel VARCHAR(2000) NOT NULL,
 original_price DECIMAL(10,2) NOT NULL, selling_price DECIMAL(10,2) NOT NULL,
 stock_num INT NOT NULL DEFAULT 0, tag VARCHAR(20) NOT NULL DEFAULT '',
 goods_sell_status INT NOT NULL DEFAULT 1, goods_detail_content TEXT,
 version INT NOT NULL DEFAULT 0, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(goods_category_id) REFERENCES tb_newbee_mall_goods_category(category_id),
 CHECK(stock_num >= 0), CHECK(selling_price >= 0), CHECK(original_price >= selling_price)
);
CREATE TABLE IF NOT EXISTS tb_newbee_mall_carousel (
 carousel_id BIGINT PRIMARY KEY AUTO_INCREMENT, carousel_url VARCHAR(200) NOT NULL,
 redirect_url VARCHAR(500) NOT NULL DEFAULT '/', carousel_rank INT NOT NULL DEFAULT 0,
 is_deleted INT NOT NULL DEFAULT 0, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS tb_newbee_mall_index_config (
 config_id BIGINT PRIMARY KEY AUTO_INCREMENT, config_name VARCHAR(50) NOT NULL,
 config_type INT NOT NULL, goods_id BIGINT NOT NULL, redirect_url VARCHAR(500) NOT NULL DEFAULT '/',
 config_rank INT NOT NULL DEFAULT 0, is_deleted INT NOT NULL DEFAULT 0,
 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(config_type, goods_id), FOREIGN KEY(goods_id) REFERENCES tb_newbee_mall_goods_info(goods_id)
);
-- The user table follows the reference naming. A must explicitly adopt this contract before integration.
CREATE TABLE IF NOT EXISTS tb_newbee_mall_user (
 user_id BIGINT PRIMARY KEY AUTO_INCREMENT, nick_name VARCHAR(50) NOT NULL,
 login_name VARCHAR(50) NOT NULL UNIQUE, password_md5 VARCHAR(100) NOT NULL DEFAULT '',
 introduce_sign VARCHAR(100) NOT NULL DEFAULT '', address VARCHAR(200) NOT NULL DEFAULT '',
 is_deleted INT NOT NULL DEFAULT 0, locked_flag INT NOT NULL DEFAULT 0,
 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS tb_member_b_guard (id INT PRIMARY KEY);
INSERT IGNORE INTO tb_member_b_guard(id) VALUES(1);
CREATE TABLE IF NOT EXISTS tb_member_b_request (
 actor_id BIGINT NOT NULL, request_key VARCHAR(100) NOT NULL, fingerprint VARCHAR(64) NOT NULL,
 response_body MEDIUMTEXT NOT NULL, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(actor_id,request_key)
);
CREATE TABLE IF NOT EXISTS tb_member_b_stock_event (
 event_id VARCHAR(100) PRIMARY KEY, goods_id BIGINT NOT NULL, quantity INT NOT NULL,
 event_kind VARCHAR(10) NOT NULL, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(goods_id) REFERENCES tb_newbee_mall_goods_info(goods_id)
);
