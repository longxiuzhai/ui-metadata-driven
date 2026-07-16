CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'demo',
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_user_tenant_username UNIQUE (tenant_id, username),
    CONSTRAINT uk_sys_user_email UNIQUE (email)
);

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_role_code UNIQUE (code)
);

CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action_name VARCHAR(64) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_permission_code UNIQUE (code)
);

CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    path VARCHAR(255) NOT NULL,
    icon VARCHAR(64),
    sort_order INT NOT NULL DEFAULT 0,
    permission_code VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_menu_code UNIQUE (code)
);

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
);

CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
);

CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    CONSTRAINT uk_sys_role_menu UNIQUE (role_id, menu_id),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
);

CREATE INDEX idx_sys_user_role_role ON sys_user_role(role_id);
CREATE INDEX idx_sys_role_permission_permission ON sys_role_permission(permission_id);
CREATE INDEX idx_sys_role_menu_menu ON sys_role_menu(menu_id);
CREATE INDEX idx_sys_menu_parent ON sys_menu(parent_id);
