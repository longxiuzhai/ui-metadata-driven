CREATE TABLE biz_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    union_id VARCHAR(128),
    mobile VARCHAR(32),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_biz_customer_tenant_customer UNIQUE (tenant_id, customer_id),
    CONSTRAINT uk_biz_customer_tenant_union UNIQUE (tenant_id, union_id)
);

CREATE TABLE biz_customer_friend_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_pk_id BIGINT NOT NULL,
    tag_name VARCHAR(64) NOT NULL,
    color VARCHAR(16) NOT NULL DEFAULT '#64748b',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_customer_friend_tag UNIQUE (customer_pk_id, tag_name),
    CONSTRAINT fk_friend_tag_customer FOREIGN KEY (customer_pk_id) REFERENCES biz_customer(id) ON DELETE CASCADE
);

CREATE TABLE biz_customer_behavior_trace (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_pk_id BIGINT NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    event_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_behavior_trace_customer FOREIGN KEY (customer_pk_id) REFERENCES biz_customer(id) ON DELETE CASCADE
);

CREATE TABLE biz_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    member_id VARCHAR(64) NOT NULL,
    union_id VARCHAR(128),
    mobile VARCHAR(32),
    member_level VARCHAR(32),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_biz_member_tenant_member UNIQUE (tenant_id, member_id),
    CONSTRAINT uk_biz_member_tenant_union UNIQUE (tenant_id, union_id)
);

CREATE TABLE biz_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    member_id VARCHAR(64),
    order_amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    order_status VARCHAR(32) NOT NULL,
    placed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_biz_order_tenant_order UNIQUE (tenant_id, order_no),
    CONSTRAINT fk_biz_order_customer FOREIGN KEY (tenant_id, customer_id)
        REFERENCES biz_customer(tenant_id, customer_id),
    CONSTRAINT fk_biz_order_member FOREIGN KEY (tenant_id, member_id)
        REFERENCES biz_member(tenant_id, member_id)
);

CREATE INDEX idx_friend_tag_customer ON biz_customer_friend_tag(customer_pk_id, sort_order);
CREATE INDEX idx_behavior_trace_customer ON biz_customer_behavior_trace(customer_pk_id, occurred_at);
CREATE INDEX idx_biz_order_customer ON biz_order(tenant_id, customer_id, placed_at);
CREATE INDEX idx_biz_order_member ON biz_order(tenant_id, member_id);

INSERT INTO biz_customer (id, tenant_id, customer_id, name, union_id, mobile, status, version)
VALUES (1001, 'demo', '1001', '示例客户 1001', 'o_demo_union_1001', '13800138000', 'ACTIVE', 1),
       (1002, 'demo', 'action-test', '动作测试客户', 'o_demo_union_action_test', '13800138001', 'ACTIVE', 1);

INSERT INTO biz_customer_friend_tag (customer_pk_id, tag_name, color, sort_order)
VALUES (1001, '重点客户', '#2563eb', 10),
       (1001, '高活跃', '#059669', 20),
       (1001, '华东区', '#7c3aed', 30);

INSERT INTO biz_customer_behavior_trace (customer_pk_id, occurred_at, event_name, description)
VALUES (1001, '2026-07-15 14:20:00', '浏览产品', '查看了企业版方案'),
       (1001, '2026-07-14 09:10:00', '销售跟进', '电话沟通需求');

INSERT INTO biz_member (tenant_id, member_id, union_id, mobile, member_level, status)
VALUES ('demo', 'M10001', 'o_demo_union_1001', '13800138000', 'GOLD', 'ACTIVE'),
       ('demo', 'M10002', 'o_demo_union_1002', '13900139000', 'SILVER', 'ACTIVE');

INSERT INTO biz_order (tenant_id, order_no, customer_id, member_id, order_amount, order_status, placed_at)
VALUES ('demo', 'O20260715001', '1001', 'M10001', 2999.00, 'PAID', '2026-07-15 16:30:00'),
       ('demo', 'O20260710002', '1001', 'M10001', 599.00, 'OPEN', '2026-07-10 11:20:00');
