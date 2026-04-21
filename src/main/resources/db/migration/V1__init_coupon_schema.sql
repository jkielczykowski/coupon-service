CREATE TABLE coupon (
    id UUID PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    max_usage_count INTEGER NOT NULL,
    current_usage_count INTEGER NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_coupon_max_usage_count_positive CHECK (max_usage_count > 0),
    CONSTRAINT chk_coupon_current_usage_count_non_negative CHECK (current_usage_count >= 0),
    CONSTRAINT chk_coupon_current_usage_count_not_greater_than_max
        CHECK (current_usage_count <= max_usage_count)
);

CREATE UNIQUE INDEX uq_coupon_code_lower
    ON coupon (LOWER(code));

CREATE TABLE coupon_redemption (
    id UUID PRIMARY KEY,
    coupon_id UUID NOT NULL,
    user_id UUID NOT NULL,
    redeemed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    requester_ip VARCHAR(64) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    CONSTRAINT fk_coupon_redemption_coupon
        FOREIGN KEY (coupon_id) REFERENCES coupon (id)
);

CREATE UNIQUE INDEX uq_coupon_redemption_coupon_user
    ON coupon_redemption (coupon_id, user_id);

CREATE INDEX idx_coupon_redemption_coupon_id
    ON coupon_redemption (coupon_id);