package com.example.ddd.infrastructure.dao.po;

import com.example.ddd.domain.user.User;
import com.example.ddd.domain.user.UserStatus;
import com.example.ddd.domain.user.valueobj.Email;
import com.example.ddd.domain.user.valueobj.Password;
import com.example.ddd.domain.user.valueobj.UserId;
import com.example.ddd.domain.user.valueobj.UserUuid;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户账户持久化对象
 * 对应表：user_account
 */
@Getter
@Setter
public class UserAccountPO {
    /**
     * 雪花ID，主键
     */
    private Long id;

    /**
     * 对外UUID，业务ID
     */
    private String uuid;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户展示名称（昵称）
     */
    private String displayName;

    /**
     * 密码哈希值
     */
    private String passwordHash;

    /**
     * 密码哈希盐
     */
    private String passwordSalt;

    /**
     * 账号状态：1正常，0冻结，2注销等
     */
    private Integer status;

    /**
     * 邮箱是否验证：0未验证，1已验证
     */
    private Integer isEmailVerified;

    /**
     * 创建时间（毫秒时间戳，UTC）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳，UTC）
     */
    private Long updatedAt;

    /**
     * 最近登录时间（毫秒时间戳）
     */
    private Long lastLoginAt;

    /**
     * 逻辑删除：0正常 1已删除
     */
    private Integer deleted;

    /**
     * 转换为领域对象 User
     *
     * @return User 领域对象
     */
    public User toDomain() {
        if (this.id == null || this.uuid == null) {
            throw new IllegalArgumentException("用户ID和UUID不能为空");
        }

        User user = new User(
                new UserId(this.id),
                new UserUuid(this.uuid)
        );

        // 设置邮箱（可能为空）
        if (this.email != null && !this.email.isEmpty()) {
            user.setEmail(new Email(this.email));
        }

        user.setDisplayName(this.displayName);

        // 设置密码（可能为空，如第三方登录）
        if (this.passwordHash != null && this.passwordSalt != null) {
            user.setPassword(new Password(this.passwordHash, this.passwordSalt));
        }

        // 转换状态
        if (this.status != null) {
            UserStatus userStatus = UserStatus.fromCode(this.status);
            user.setStatus(userStatus != null ? userStatus : UserStatus.NORMAL);
        } else {
            user.setStatus(UserStatus.NORMAL);
        }

        // 转换邮箱验证状态
        user.setEmailVerified(this.isEmailVerified != null && this.isEmailVerified == 1);

        // 设置时间戳
        user.setCreatedAt(this.createdAt != null ? this.createdAt : 0);
        user.setUpdatedAt(this.updatedAt != null ? this.updatedAt : 0);
        user.setLastLoginAt(this.lastLoginAt);

        // 转换逻辑删除状态
        user.setDeleted(this.deleted != null && this.deleted == 1);

        return user;
    }

    /**
     * 从领域对象 User 创建 PO
     *
     * @param user 用户领域对象
     * @return UserAccountPO 持久化对象
     */
    public static UserAccountPO fromDomain(User user) {
        if (user == null) {
            return null;
        }

        UserAccountPO po = new UserAccountPO();
        po.setId(user.getId().getValue());
        po.setUuid(user.getUuid().getValue());

        // 设置邮箱（可能为空）
        if (user.getEmail() != null) {
            po.setEmail(user.getEmail().getValue());
        }

        po.setDisplayName(user.getDisplayName());

        // 设置密码（可能为空）
        if (user.getPassword() != null) {
            po.setPasswordHash(user.getPassword().getHash());
            po.setPasswordSalt(user.getPassword().getSalt());
        }

        // 转换状态
        if (user.getStatus() != null) {
            po.setStatus(user.getStatus().getCode());
        } else {
            po.setStatus(UserStatus.NORMAL.getCode());
        }

        // 转换邮箱验证状态
        po.setIsEmailVerified(user.isEmailVerified() ? 1 : 0);

        // 设置时间戳
        po.setCreatedAt(user.getCreatedAt());
        po.setUpdatedAt(user.getUpdatedAt());
        po.setLastLoginAt(user.getLastLoginAt());

        // 转换逻辑删除状态
        po.setDeleted(user.isDeleted() ? 1 : 0);

        return po;
    }
}

