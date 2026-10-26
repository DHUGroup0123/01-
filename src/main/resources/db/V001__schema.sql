-- 01电商系统 V001 baseline. No DROP, DELETE, TRUNCATE or implicit database creation.
-- MySQL 8.0.16+; the same subset is exercised by H2 in MySQL compatibility mode.

CREATE TABLE IF NOT EXISTS schema_version (
    version VARCHAR(20) PRIMARY KEY,
    description VARCHAR(200) NOT NULL,
    installed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mall_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_name VARCHAR(32) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(40) NOT NULL,
    phone VARCHAR(20),
    status TINYINT NOT NULL DEFAULT 0 CHECK (status IN (0,1)),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_address (
    address_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(40) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(40) NOT NULL,
    city VARCHAR(40) NOT NULL,
    district VARCHAR(40) NOT NULL,
    detail_address VARCHAR(200) NOT NULL,
    is_default TINYINT NOT NULL DEFAULT 0 CHECK (is_default IN (0,1)),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES mall_user(user_id)
);

CREATE TABLE IF NOT EXISTS admin_user (
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_name VARCHAR(32) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(40) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 CHECK (status IN (0,1)),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT,
    category_level TINYINT NOT NULL CHECK (category_level BETWEEN 1 AND 3),
    category_name VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0 CHECK (is_deleted IN (0,1)),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES category(category_id)
);

CREATE TABLE IF NOT EXISTS product (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    summary VARCHAR(250) NOT NULL,
    cover_url VARCHAR(500),
    detail_html TEXT,
    original_price DECIMAL(10,2) NOT NULL CHECK (original_price >= 0),
    selling_price DECIMAL(10,2) NOT NULL CHECK (selling_price >= 0),
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    sell_status TINYINT NOT NULL DEFAULT 0 CHECK (sell_status IN (0,1)),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(category_id)
);

CREATE TABLE IF NOT EXISTS carousel (
    carousel_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_url VARCHAR(500) NOT NULL,
    redirect_url VARCHAR(500),
    sort_order INT NOT NULL DEFAULT 0,
    is_enabled TINYINT NOT NULL DEFAULT 1 CHECK (is_enabled IN (0,1)),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS home_product_config (
    config_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_type TINYINT NOT NULL CHECK (config_type IN (1,2,3)),
    product_id BIGINT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_enabled TINYINT NOT NULL DEFAULT 1 CHECK (is_enabled IN (0,1)),
    CONSTRAINT uq_home_product UNIQUE (config_type,product_id),
    CONSTRAINT fk_home_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE IF NOT EXISTS cart_item (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity BETWEEN 1 AND 99),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cart_product UNIQUE (user_id,product_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES mall_user(user_id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE IF NOT EXISTS mall_order (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(40) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),
    order_status INT NOT NULL DEFAULT 0 CHECK (order_status IN (0,10,20,30,40,-10,-20,-30)),
    pay_status TINYINT NOT NULL DEFAULT 0 CHECK (pay_status IN (0,1)),
    pay_method VARCHAR(20) NOT NULL DEFAULT 'DEMO',
    receiver_name VARCHAR(40) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(350) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES mall_user(user_id)
);

CREATE TABLE IF NOT EXISTS order_item (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    cover_url VARCHAR(500),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    quantity INT NOT NULL CHECK (quantity BETWEEN 1 AND 99),
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES mall_order(order_id),
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);
