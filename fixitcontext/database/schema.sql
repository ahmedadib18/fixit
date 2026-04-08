-- ============================================================
-- I Can Fix It - Database Schema (H2 Compatible)
-- Based strictly on Requirements and Wireframes
-- ============================================================

-- 0A. COUNTRIES (data from CountryStateCity API)
CREATE TABLE IF NOT EXISTS countries (
    id         BIGINT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    iso2       VARCHAR(2)  NOT NULL UNIQUE,
    iso3       VARCHAR(3),
    phone_code VARCHAR(10)
);

-- 0B. CITIES (data from CountryStateCity API)
CREATE TABLE IF NOT EXISTS cities (
    id         BIGINT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    country_id BIGINT NOT NULL,
    state_name VARCHAR(100),
    CONSTRAINT fk_cities_country FOREIGN KEY (country_id) REFERENCES countries(id)
);

-- 1. USERS (WF01, WF03, WF10, Req1)
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    city_id         BIGINT,
    user_type       VARCHAR(10) NOT NULL CHECK (user_type IN ('USER', 'HELPER', 'ADMIN')),
    profile_image_url VARCHAR(500),
    account_status  VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'BANNED')),
    google_id       VARCHAR(255),
    oauth_provider  VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login      TIMESTAMP,
    CONSTRAINT fk_users_city FOREIGN KEY (city_id) REFERENCES cities(id)
);

-- 2. HELPERS (WF02, WF04, Req2)
CREATE TABLE IF NOT EXISTS helpers (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id               BIGINT NOT NULL UNIQUE,
    professional_headline VARCHAR(255),
    languages_spoken      VARCHAR(500),
    is_available          BOOLEAN DEFAULT FALSE,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_helpers_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 3. CATEGORIES (WF02, WF04, Req2, Req4)
CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    is_active   BOOLEAN DEFAULT TRUE
);

-- 4. HELPER_CATEGORIES (WF02, Req2)
CREATE TABLE IF NOT EXISTS helper_categories (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    helper_id       BIGINT NOT NULL,
    category_id     BIGINT NOT NULL,
    hourly_rate     DECIMAL(10, 2),
    fixed_rate      DECIMAL(10, 2),
    years_experience INT,
    certificate_url VARCHAR(500),
    CONSTRAINT fk_hc_helper FOREIGN KEY (helper_id) REFERENCES helpers(id),
    CONSTRAINT fk_hc_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT uk_helper_category UNIQUE (helper_id, category_id)
);

-- 5. HELPER_AVAILABILITIES (WF04, Req2-AC3)
CREATE TABLE IF NOT EXISTS helper_availabilities (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    helper_id   BIGINT NOT NULL,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    start_time  TIME NOT NULL,
    end_time    TIME NOT NULL,
    timezone    VARCHAR(50) DEFAULT 'UTC',
    is_active   BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_ha_helper FOREIGN KEY (helper_id) REFERENCES helpers(id)
);

-- 6. PAYMENT_METHODS (WF03, WF06, Req3)
CREATE TABLE IF NOT EXISTS payment_methods (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    stripe_payment_method_id VARCHAR(255) NOT NULL,
    card_last_four          VARCHAR(4),
    card_brand              VARCHAR(50),
    is_default              BOOLEAN DEFAULT FALSE,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pm_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 7. SESSIONS (WF05, WF06, WF08, Req5, Req6, Req9, Req11)
CREATE TABLE IF NOT EXISTS sessions (
    id                      VARCHAR(36) PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    helper_id               BIGINT NOT NULL,
    category_id             BIGINT,
    status                  VARCHAR(20) NOT NULL CHECK (status IN ('INITIATED', 'CONNECTED', 'IN_PROGRESS', 'PAUSED', 'ENDED', 'CANCELLED')),
    started_at              TIMESTAMP,
    ended_at                TIMESTAMP,
    helper_rate             DECIMAL(10, 2),
    user_consent_public     BOOLEAN DEFAULT FALSE,
    helper_consent_public   BOOLEAN DEFAULT FALSE,
    retention_months        INT DEFAULT 12,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_sessions_helper FOREIGN KEY (helper_id) REFERENCES helpers(id),
    CONSTRAINT fk_sessions_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- 8. SESSION_CHAT_MESSAGES (WF05, WF08, Req5-AC2, Req6-AC3)
CREATE TABLE IF NOT EXISTS session_chat_messages (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id   VARCHAR(36) NOT NULL,
    sender_id    BIGINT NOT NULL,
    message_text TEXT,
    message_type VARCHAR(10) NOT NULL CHECK (message_type IN ('TEXT', 'FILE', 'SYSTEM')),
    file_url     VARCHAR(500),
    sent_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_scm_session FOREIGN KEY (session_id) REFERENCES sessions(id),
    CONSTRAINT fk_scm_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- 9. TRANSACTIONS (WF06, Req7)
CREATE TABLE IF NOT EXISTS transactions (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id               VARCHAR(36) NOT NULL,
    payment_method_id        BIGINT,
    stripe_payment_intent_id VARCHAR(255),
    amount                   DECIMAL(10, 2) NOT NULL,
    platform_fee             DECIMAL(10, 2),
    currency                 VARCHAR(3) DEFAULT 'USD',
    status                   VARCHAR(15) NOT NULL CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED')),
    processed_at             TIMESTAMP,
    refunded_at              TIMESTAMP,
    refund_reason            VARCHAR(500),
    CONSTRAINT fk_transactions_session FOREIGN KEY (session_id) REFERENCES sessions(id),
    CONSTRAINT fk_transactions_pm FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
);

-- 10. RECEIPTS (WF06, Req7-AC4)
CREATE TABLE IF NOT EXISTS receipts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id  BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    helper_id       BIGINT NOT NULL,
    receipt_number  VARCHAR(50) NOT NULL UNIQUE,
    receipt_data    TEXT,
    generated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_receipts_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT fk_receipts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_receipts_helper FOREIGN KEY (helper_id) REFERENCES helpers(id)
);

-- 11. REVIEWS (WF07, Req8)
CREATE TABLE IF NOT EXISTS reviews (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id      VARCHAR(36) NOT NULL,
    user_id         BIGINT NOT NULL,
    helper_id       BIGINT NOT NULL,
    rating          INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review_text     TEXT,
    is_public       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_session FOREIGN KEY (session_id) REFERENCES sessions(id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_helper FOREIGN KEY (helper_id) REFERENCES helpers(id)
);

-- 12. SUPPORT_TICKETS (WF09, Req10-AC3)
CREATE TABLE IF NOT EXISTS support_tickets (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    session_id      VARCHAR(36),
    subject         VARCHAR(255) NOT NULL,
    description     TEXT NOT NULL,
    status          VARCHAR(15) NOT NULL CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    assigned_admin_id BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at     TIMESTAMP,
    CONSTRAINT fk_st_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_st_session FOREIGN KEY (session_id) REFERENCES sessions(id),
    CONSTRAINT fk_st_admin FOREIGN KEY (assigned_admin_id) REFERENCES users(id)
);

-- 13. SUPPORT_TICKET_RESPONSES (WF09)
CREATE TABLE IF NOT EXISTS support_ticket_responses (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id     BIGINT NOT NULL,
    responder_id  BIGINT NOT NULL,
    response_text TEXT NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_str_ticket FOREIGN KEY (ticket_id) REFERENCES support_tickets(id),
    CONSTRAINT fk_str_responder FOREIGN KEY (responder_id) REFERENCES users(id)
);

-- 14. DISPUTES (WF11, WF06, Req10-AC2, AC5)
CREATE TABLE IF NOT EXISTS disputes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id      VARCHAR(36) NOT NULL,
    complainant_id  BIGINT NOT NULL,
    respondent_id   BIGINT NOT NULL,
    dispute_type    VARCHAR(20) NOT NULL CHECK (dispute_type IN ('BILLING', 'SERVICE')),
    amount          DECIMAL(10, 2),
    description     TEXT NOT NULL,
    status          VARCHAR(15) NOT NULL CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED')),
    resolution      TEXT,
    refund_amount   DECIMAL(10, 2),
    assigned_admin_id BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at     TIMESTAMP,
    CONSTRAINT fk_disputes_session FOREIGN KEY (session_id) REFERENCES sessions(id),
    CONSTRAINT fk_disputes_complainant FOREIGN KEY (complainant_id) REFERENCES users(id),
    CONSTRAINT fk_disputes_respondent FOREIGN KEY (respondent_id) REFERENCES users(id),
    CONSTRAINT fk_disputes_admin FOREIGN KEY (assigned_admin_id) REFERENCES users(id)
);

-- ============================================================
-- INDEXES for performance
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_cities_country ON cities(country_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_user_type ON users(user_type);
CREATE INDEX IF NOT EXISTS idx_users_account_status ON users(account_status);
CREATE INDEX IF NOT EXISTS idx_users_city ON users(city_id);
CREATE INDEX IF NOT EXISTS idx_helpers_user ON helpers(user_id);
CREATE INDEX IF NOT EXISTS idx_helpers_available ON helpers(is_available);
CREATE INDEX IF NOT EXISTS idx_hc_helper ON helper_categories(helper_id);
CREATE INDEX IF NOT EXISTS idx_hc_category ON helper_categories(category_id);
CREATE INDEX IF NOT EXISTS idx_ha_helper ON helper_availabilities(helper_id);
CREATE INDEX IF NOT EXISTS idx_pm_user ON payment_methods(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_user ON sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_helper ON sessions(helper_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON sessions(status);
CREATE INDEX IF NOT EXISTS idx_scm_session ON session_chat_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_transactions_session ON transactions(session_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_pm ON transactions(payment_method_id);
CREATE INDEX IF NOT EXISTS idx_reviews_helper ON reviews(helper_id);
CREATE INDEX IF NOT EXISTS idx_reviews_session ON reviews(session_id);
CREATE INDEX IF NOT EXISTS idx_st_user ON support_tickets(user_id);
CREATE INDEX IF NOT EXISTS idx_st_status ON support_tickets(status);
CREATE INDEX IF NOT EXISTS idx_disputes_session ON disputes(session_id);
CREATE INDEX IF NOT EXISTS idx_disputes_status ON disputes(status);
