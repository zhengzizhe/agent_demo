-- 用户主表（账户基础信息）
CREATE TABLE user_account (
    id BIGINT PRIMARY KEY,                         -- 雪花ID，主键，应用生成，不自增

    uuid VARCHAR(64) NOT NULL UNIQUE,              -- 对外UUID，业务ID，不暴露自增/雪花规则

    email VARCHAR(255) UNIQUE,                     -- 用户邮箱，可为空（第三方登录场景）
    display_name VARCHAR(255),                     -- 用户展示名称（昵称）

    password_hash VARCHAR(255),                    -- 密码哈希值（邮箱密码登录时使用）
    password_salt VARCHAR(255),                    -- 密码哈希盐（可选）

    status SMALLINT NOT NULL DEFAULT 1,            -- 账号状态：1正常，0冻结，2注销等
    is_email_verified SMALLINT NOT NULL DEFAULT 0, -- 邮箱是否验证：0未验证，1已验证

    created_at BIGINT NOT NULL,                    -- 创建时间（毫秒时间戳，UTC）
    updated_at BIGINT NOT NULL,                    -- 更新时间（毫秒时间戳，UTC）
    last_login_at BIGINT,                          -- 最近登录时间（毫秒时间戳）

    deleted SMALLINT NOT NULL DEFAULT 0            -- 逻辑删除：0正常 1已删除
);

COMMENT ON TABLE user_account IS '用户主表（账户基础信息）';
COMMENT ON COLUMN user_account.id IS '全局唯一雪花ID，作为主键，不对外暴露';
COMMENT ON COLUMN user_account.uuid IS '对外唯一用户标识（UUID/短UUID），避免暴露内部ID';
COMMENT ON COLUMN user_account.email IS '用户邮箱，支持为空（第三方登录用户可能没有邮箱）';
COMMENT ON COLUMN user_account.display_name IS '用户展示名称（昵称）';
COMMENT ON COLUMN user_account.password_hash IS '密码哈希值（邮箱密码登录时使用）';
COMMENT ON COLUMN user_account.password_salt IS '密码哈希盐（可选）';
COMMENT ON COLUMN user_account.status IS '账号状态：1正常，0冻结，2注销等';
COMMENT ON COLUMN user_account.is_email_verified IS '邮箱是否验证：0未验证，1已验证';
COMMENT ON COLUMN user_account.created_at IS '创建时间（毫秒时间戳，UTC）';
COMMENT ON COLUMN user_account.updated_at IS '更新时间（毫秒时间戳，UTC）';
COMMENT ON COLUMN user_account.last_login_at IS '最近登录时间（毫秒时间戳，UTC）';
COMMENT ON COLUMN user_account.deleted IS '逻辑删除标记：0正常 1已删除';
-- 认证身份表：记录用户与各种登录方式（邮箱/第三方）的绑定关系
CREATE TABLE auth_identity (
    id BIGINT PRIMARY KEY,                     -- 雪花ID，主键

    user_id BIGINT NOT NULL,                   -- 关联 user_account.id，表示属于哪个用户

    provider VARCHAR(50) NOT NULL,             -- 认证提供方：local_email / google / github / wechat / sso_company 等
    provider_uid VARCHAR(255) NOT NULL,        -- 在该 provider 下的唯一标识（如 openid/sub/email）

    extra_info JSONB,                          -- 扩展信息，如头像、token、unionid 等

    created_at BIGINT NOT NULL,                -- 创建时间（毫秒时间戳，UTC）
    updated_at BIGINT NOT NULL,                -- 更新时间（毫秒时间戳，UTC）

    CONSTRAINT uq_provider_uid UNIQUE (provider, provider_uid),
    CONSTRAINT fk_auth_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE INDEX idx_auth_user ON auth_identity (user_id);

COMMENT ON TABLE auth_identity IS '认证身份表（用户与各种登录方式映射关系）';
COMMENT ON COLUMN auth_identity.id IS '雪花ID，主键';
COMMENT ON COLUMN auth_identity.user_id IS '用户ID，关联 user_account.id';
COMMENT ON COLUMN auth_identity.provider IS '认证提供方：local_email / google / github / wechat / sso_company 等';
COMMENT ON COLUMN auth_identity.provider_uid IS '在该认证提供方下的唯一用户标识（如 openid/sub/email）';
COMMENT ON COLUMN auth_identity.extra_info IS '扩展信息，JSONB 存储三方返回的额外字段，如头像、unionid、token 等';
COMMENT ON COLUMN auth_identity.created_at IS '创建时间（毫秒时间戳，UTC）';
COMMENT ON COLUMN auth_identity.updated_at IS '更新时间（毫秒时间戳，UTC）';
-- 邮箱验证码记录表：用于注册/登录/找回密码等场景
CREATE TABLE email_verification (
    id BIGINT PRIMARY KEY,                 -- 雪花ID，主键

    email VARCHAR(255) NOT NULL,           -- 接收验证码的邮箱
    code VARCHAR(16) NOT NULL,             -- 验证码内容
    purpose VARCHAR(32) NOT NULL,          -- 验证码用途：register / login / reset_password / bind_email 等

    status SMALLINT NOT NULL DEFAULT 0,    -- 验证码状态：0未使用，1已使用，2已失效

    expire_at BIGINT NOT NULL,             -- 验证码过期时间（毫秒时间戳）
    created_at BIGINT NOT NULL,            -- 创建时间（毫秒时间戳）
    used_at BIGINT,                        -- 使用时间（毫秒时间戳）

    client_ip VARCHAR(64),                 -- 请求验证码的客户端 IP
    user_agent TEXT,                       -- 请求时的 UA 信息

    CONSTRAINT idx_email_purpose UNIQUE (email, code, purpose, created_at)
);

CREATE INDEX idx_email_purpose_only ON email_verification (email, purpose);

COMMENT ON TABLE email_verification IS '邮箱验证码记录表';
COMMENT ON COLUMN email_verification.id IS '雪花ID，主键';
COMMENT ON COLUMN email_verification.email IS '接收验证码的邮箱地址';
COMMENT ON COLUMN email_verification.code IS '验证码内容';
COMMENT ON COLUMN email_verification.purpose IS '验证码用途：register / login / reset_password / bind_email 等';
COMMENT ON COLUMN email_verification.status IS '验证码状态：0未使用，1已使用，2已失效';
COMMENT ON COLUMN email_verification.expire_at IS '验证码过期时间（毫秒时间戳）';
COMMENT ON COLUMN email_verification.created_at IS '验证码创建时间（毫秒时间戳）';
COMMENT ON COLUMN email_verification.used_at IS '验证码实际使用时间（毫秒时间戳）';
COMMENT ON COLUMN email_verification.client_ip IS '请求验证码的客户端IP';
COMMENT ON COLUMN email_verification.user_agent IS '请求验证码时的客户端 User-Agent 信息';
-- 用户登录日志表：记录所有登录行为，便于审计与风控
CREATE TABLE login_log (
    id BIGINT PRIMARY KEY,                 -- 雪花ID，主键

    user_id BIGINT,                        -- 登录对应的用户ID，可为空（如账号不存在时的失败登录）
    provider VARCHAR(50) NOT NULL,         -- 登录使用的认证提供方：local_email / google / github / wechat / email_code 等
    login_type VARCHAR(32) NOT NULL,       -- 登录类型：password / email_code / oauth / sso 等

    success SMALLINT NOT NULL,             -- 是否成功：1成功，0失败
    fail_reason VARCHAR(255),              -- 失败原因说明：wrong_password / invalid_code / locked 等

    ip_address VARCHAR(64),                -- 登录IP
    user_agent TEXT,                       -- 登录设备UA

    created_at BIGINT NOT NULL,            -- 日志创建时间（毫秒时间戳）

    CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE INDEX idx_login_user ON login_log (user_id);
CREATE INDEX idx_login_created_at ON login_log (created_at);

COMMENT ON TABLE login_log IS '用户登录日志表';
COMMENT ON COLUMN login_log.id IS '雪花ID，主键';
COMMENT ON COLUMN login_log.user_id IS '登录对应的用户ID，可为空（如账号不存在导致的失败登录）';
COMMENT ON COLUMN login_log.provider IS '登录使用的认证提供方：local_email / google / github / wechat / email_code 等';
COMMENT ON COLUMN login_log.login_type IS '登录类型：password / email_code / oauth / sso 等';
COMMENT ON COLUMN login_log.success IS '登录是否成功：1成功，0失败';
COMMENT ON COLUMN login_log.fail_reason IS '登录失败原因，如 wrong_password / invalid_code / locked 等';
COMMENT ON COLUMN login_log.ip_address IS '登录来源IP地址';
COMMENT ON COLUMN login_log.user_agent IS '登录请求的User-Agent信息';
COMMENT ON COLUMN login_log.created_at IS '登录日志创建时间（毫秒时间戳）';
