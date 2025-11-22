package com.example.ddd.domain.user;

import com.example.ddd.domain.user.enetity.AuthIdentity;
import com.example.ddd.domain.user.valueobj.Email;
import com.example.ddd.domain.user.valueobj.Password;
import com.example.ddd.domain.user.valueobj.UserId;
import com.example.ddd.domain.user.valueobj.UserUuid;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户聚合根（User Aggregate Root）
 * 对应表：user_account
 */
@Getter
@Setter
@Data
public class User {

    /** 
     * 用户内部唯一标识（雪花ID）
     * 对应 user_account.id
     */
    private final UserId id;

    /**
     * 对外唯一公开标识（UUID/短UUID）
     * 对应 user_account.uuid
     */
    private final UserUuid uuid;

    /**
     * 用户邮箱（可为空：第三方登录可能没有邮箱）
     * 对应 user_account.email
     */
    private Email email;

    /**
     * 用户展示名称（昵称）
     * 对应 user_account.display_name
     */
    private String displayName;

    /**
     * 用户密码（包含 hash 和 salt）
     * 对应 user_account.password_hash, password_salt
     */
    private Password password;

    /**
     * 用户状态（正常/冻结/注销等）
     * 对应 user_account.status
     */
    private UserStatus status;

    /**
     * 邮箱是否已验证
     * 对应 user_account.is_email_verified
     */
    private boolean emailVerified;

    /**
     * 账号创建时间（毫秒时间戳，UTC）
     * 对应 user_account.created_at
     */
    private long createdAt;

    /**
     * 账号更新时间（毫秒时间戳，UTC）
     * 对应 user_account.updated_at
     */
    private long updatedAt;

    /**
     * 最近登录时间（毫秒时间戳）
     * 对应 user_account.last_login_at
     */
    private Long lastLoginAt;

    /**
     * 是否逻辑删除（0正常 1删除）
     * 对应 user_account.deleted
     */
    private boolean deleted;

    /**
     * 用户所有的认证身份（第三方登录、邮箱登录等）
     * 一对多，对应 auth_identity 表
     */
    private List<AuthIdentity> identities;

    /**
     * 构造函数
     * id 和 uuid 是必需的不变字段
     *
     * @param id   用户ID
     * @param uuid 用户UUID
     */
    public User(UserId id, UserUuid uuid) {
        if (id == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (uuid == null) {
            throw new IllegalArgumentException("用户UUID不能为空");
        }
        this.id = id;
        this.uuid = uuid;
    }

    // ==== 行为方法省略，你可以按需要扩展 ====
}
