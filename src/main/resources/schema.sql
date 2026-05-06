CREATE TABLE IF NOT EXISTS tb_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(128) NULL,
    time_coin DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    frozen_coin DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    credit_score INT NOT NULL DEFAULT 100,
    campus VARCHAR(128) NULL,
    profile VARCHAR(255) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS skill_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    description VARCHAR(500) NOT NULL,
    price_per_hour DECIMAL(10, 2) NOT NULL,
    category VARCHAR(64) NOT NULL,
    capacity INT NOT NULL DEFAULT 1,
    status INT NOT NULL DEFAULT 0,
    tags VARCHAR(255) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_skill_owner FOREIGN KEY (owner_id) REFERENCES tb_user(id)
);

CREATE TABLE IF NOT EXISTS trade_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    skill_item_id BIGINT NOT NULL,
    hours DECIMAL(10, 2) NOT NULL,
    total_coin DECIMAL(10, 2) NOT NULL,
    status INT NOT NULL,
    start_time DATETIME NULL,
    end_time DATETIME NULL,
    remark VARCHAR(255) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_buyer FOREIGN KEY (buyer_id) REFERENCES tb_user(id),
    CONSTRAINT fk_order_seller FOREIGN KEY (seller_id) REFERENCES tb_user(id),
    CONSTRAINT fk_order_skill FOREIGN KEY (skill_item_id) REFERENCES skill_item(id)
);

CREATE TABLE IF NOT EXISTS wallet_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    transaction_type VARCHAR(32) NOT NULL,
    change_amount DECIMAL(10, 2) NOT NULL,
    balance_after DECIMAL(10, 2) NOT NULL,
    remark VARCHAR(255) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES tb_user(id),
    CONSTRAINT fk_wallet_order FOREIGN KEY (order_id) REFERENCES trade_order(id)
);

CREATE TABLE IF NOT EXISTS review_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    skill_item_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    rating INT NOT NULL,
    content VARCHAR(500) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES trade_order(id),
    CONSTRAINT fk_review_skill FOREIGN KEY (skill_item_id) REFERENCES skill_item(id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES tb_user(id),
    CONSTRAINT fk_review_reviewee FOREIGN KEY (reviewee_id) REFERENCES tb_user(id)
);
