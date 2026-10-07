-- =====================================================
-- IPOS-PU Database Schema
-- Nova Solutions — Team 24/C
-- =====================================================
-- Run this script to create all tables for the IPOS-PU subsystem.
-- Usage: mysql -u root -p ipos_pu < ipos_pu_schema.sql
--
-- Tables (mapped to use cases):
--   members                 UC-01a/b, UC-02, UC-03
--   commercial_applications UC-01b
--   products                UC-04, UC-05, UC-06
--   cart_items              UC-06, UC-07
--   orders                  UC-08, UC-09, UC-10
--   order_items             UC-08, UC-09
--   payments                UC-12
--   refunds                 UC-10
--   email_queue             UC-11 (store-and-retry when SMTP fails)
--   promotion_campaigns     UC-13, UC-14, UC-15, UC-16
--   campaign_products       UC-13, UC-16
--   campaign_tracking       UC-15, UC-16, UC-19
-- =====================================================

-- ─── UC-01a, UC-01b, UC-02, UC-03 ───────────────────────────────────────────

CREATE TABLE IF NOT EXISTS members (
    id                 BIGINT          AUTO_INCREMENT PRIMARY KEY,
    full_name          VARCHAR(120)    NOT NULL,
    email              VARCHAR(120)    NOT NULL UNIQUE,
    password_hash      VARCHAR(255)    NOT NULL,
    member_type        ENUM('NON_COMMERCIAL', 'COMMERCIAL', 'ADMIN') NOT NULL,
    membership_status  ENUM('APPROVED', 'PENDING', 'REJECTED')       NOT NULL,
    company_name       VARCHAR(120)    NULL,         -- commercial members only
    order_count        INT             NOT NULL DEFAULT 0, -- tracks 10th-order loyalty discount
    is_first_login     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at         DATETIME(6)     NOT NULL
);

-- ─── UC-01b ──────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS commercial_applications (
    id                       BIGINT          AUTO_INCREMENT PRIMARY KEY,
    member_id                BIGINT          NULL,    -- set after approval creates member
    companies_house_number   VARCHAR(20)     NOT NULL,
    director_names           VARCHAR(255)    NOT NULL,
    business_type            VARCHAR(100)    NOT NULL,
    business_address         VARCHAR(255)    NOT NULL,
    email                    VARCHAR(120)    NOT NULL,
    status                   ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    submitted_at             DATETIME(6)     NOT NULL,
    reviewed_at              DATETIME(6)     NULL,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

-- ─── UC-04, UC-05, UC-06 ─────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS products (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(160)    NOT NULL,
    description     VARCHAR(1200)   NOT NULL,
    price           DECIMAL(10,2)   NOT NULL,
    stock_quantity  INT             NOT NULL DEFAULT 0,
    active          BIT(1)          NOT NULL DEFAULT 1,
    created_at      DATETIME(6)     NOT NULL
);

-- ─── UC-06, UC-07 ────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS cart_items (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT          NULL,    -- NULL for guest checkout
    session_id  VARCHAR(64)     NULL,    -- used to identify guest carts
    product_id  BIGINT          NOT NULL,
    quantity    INT             NOT NULL,
    added_at    DATETIME(6)     NOT NULL,
    FOREIGN KEY (member_id)  REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ─── UC-08, UC-09, UC-10 ─────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT          NULL,    -- NULL for guest orders
    guest_email      VARCHAR(120)    NULL,
    guest_address    VARCHAR(255)    NULL,
    status           ENUM('RECEIVED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'REFUND_REQUESTED', 'REFUNDED') NOT NULL DEFAULT 'RECEIVED',
    total_amount     DECIMAL(10,2)   NOT NULL,
    discount_amount  DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    notes            VARCHAR(1000)   NULL,
    created_at       DATETIME(6)     NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_items (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT          NOT NULL,
    product_id  BIGINT          NOT NULL,
    quantity    INT             NOT NULL,
    unit_price  DECIMAL(10,2)   NOT NULL,
    line_total  DECIMAL(10,2)   NOT NULL,
    FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ─── UC-12 ───────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS payments (
    id                   BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id             BIGINT          NULL,
    amount               DECIMAL(10,2)   NOT NULL,
    card_number_masked   VARCHAR(19)     NOT NULL,  -- stores first 4 and last 4 digits only
    expiry               VARCHAR(5)      NOT NULL,  -- MM/YY
    status               ENUM('AUTHORISED', 'DECLINED', 'PENDING') NOT NULL,
    transaction_id       VARCHAR(64)     NULL,
    processed_at         DATETIME(6)     NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- ─── UC-10 ───────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS refunds (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    payment_id      BIGINT          NOT NULL,
    amount          DECIMAL(10,2)   NOT NULL,
    status          ENUM('PROCESSED', 'FAILED') NOT NULL,
    processed_at    DATETIME(6)     NOT NULL,
    FOREIGN KEY (order_id)   REFERENCES orders(id),
    FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- ─── UC-11 ───────────────────────────────────────────────────────────────────
-- Stores emails for retry when SMTP server is unavailable

CREATE TABLE IF NOT EXISTS email_queue (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    recipient   VARCHAR(120)    NOT NULL,
    subject     VARCHAR(255)    NOT NULL,
    body        TEXT            NOT NULL,
    status      ENUM('PENDING', 'SENT', 'FAILED') NOT NULL DEFAULT 'PENDING',
    created_at  DATETIME(6)     NOT NULL,
    sent_at     DATETIME(6)     NULL
);

-- ─── UC-13, UC-14, UC-15, UC-16 ──────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS promotion_campaigns (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(160)    NOT NULL,
    start_date       DATE            NOT NULL,
    end_date         DATE            NOT NULL,
    discount_percent DECIMAL(5,2)    NOT NULL,  -- uniform discount; variable set per product in campaign_products
    status           ENUM('ACTIVE', 'DRAFT', 'INACTIVE') NOT NULL DEFAULT 'DRAFT',
    target_products  VARCHAR(600)    NULL,       -- legacy field kept for compatibility
    created_at       DATETIME(6)     NOT NULL
);

-- ─── UC-13, UC-16 ────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS campaign_products (
    id           BIGINT          AUTO_INCREMENT PRIMARY KEY,
    campaign_id  BIGINT          NOT NULL,
    product_id   BIGINT          NOT NULL,
    discount_percent DECIMAL(5,2) NOT NULL,
    UNIQUE KEY unique_campaign_product (campaign_id, product_id),  -- prevents overlap conflict
    FOREIGN KEY (campaign_id) REFERENCES promotion_campaigns(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id)  REFERENCES products(id) ON DELETE CASCADE
);
