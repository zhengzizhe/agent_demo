package com.example.ddd.domain.user;

import com.example.ddd.common.utils.IdGenerator;
import com.example.ddd.domain.user.valueobj.Email;
import com.example.ddd.domain.user.valueobj.Password;
import com.example.ddd.domain.user.valueobj.UserId;
import com.example.ddd.domain.user.valueobj.UserUuid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 用户工厂类
 * 负责创建 User 聚合根对象
 */
@Component
@RequiredArgsConstructor
public class UserFactory {

    private final IdGenerator idGenerator;

    /**
     * 创建新用户（注册场景）
     * 自动生成 ID 和 UUID，设置默认状态为正常
     *
     * @param email        邮箱
     * @param displayName  显示名称
     * @param password     密码
     * @return User 对象
     */
    public User createNewUser(Email email, String displayName, Password password) {
        long userId = idGenerator.nextSnowflakeId();
        String userUuid = idGenerator.nextUuidWithoutHyphens();
        long now = System.currentTimeMillis();

        User user = new User(new UserId(userId), new UserUuid(userUuid));
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPassword(password);
        user.setStatus(UserStatus.NORMAL);
        user.setEmailVerified(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeleted(false);
        user.setIdentities(new ArrayList<>());

        return user;
    }

    /**
     * 创建新用户（第三方登录场景，可能没有邮箱和密码）
     *
     * @param displayName 显示名称
     * @return User 对象
     */
    public User createNewUserFromOAuth(String displayName) {
        long userId = idGenerator.nextSnowflakeId();
        String userUuid = idGenerator.nextUuidWithoutHyphens();
        long now = System.currentTimeMillis();

        User user = new User(new UserId(userId), new UserUuid(userUuid));
        user.setDisplayName(displayName);
        user.setStatus(UserStatus.NORMAL);
        user.setEmailVerified(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeleted(false);
        user.setIdentities(new ArrayList<>());

        return user;
    }

    /**
     * 从数据库重建用户对象（用于查询场景）
     *
     * @param id          用户ID
     * @param uuid        用户UUID
     * @param email       邮箱
     * @param displayName 显示名称
     * @param password    密码
     * @param status      状态
     * @param emailVerified 邮箱是否已验证
     * @param createdAt   创建时间
     * @param updatedAt   更新时间
     * @param lastLoginAt 最近登录时间
     * @param deleted     是否删除
     * @return User 对象
     */
    public User reconstructUser(
            long id,
            String uuid,
            Email email,
            String displayName,
            Password password,
            UserStatus status,
            boolean emailVerified,
            long createdAt,
            long updatedAt,
            Long lastLoginAt,
            boolean deleted) {
        User user = new User(new UserId(id), new UserUuid(uuid));
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPassword(password);
        user.setStatus(status);
        user.setEmailVerified(emailVerified);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);
        user.setLastLoginAt(lastLoginAt);
        user.setDeleted(deleted);
        user.setIdentities(new ArrayList<>());

        return user;
    }

    /**
     * 从数据库重建用户对象（简化版，用于查询场景）
     *
     * @param id          用户ID
     * @param uuid        用户UUID
     * @param email       邮箱
     * @param displayName 显示名称
     * @param status      状态
     * @return User 对象
     */
    public User reconstructUser(
            long id,
            String uuid,
            Email email,
            String displayName,
            UserStatus status) {
        return reconstructUser(id, uuid, email, displayName, null, status, false, 0, 0, null, false);
    }
}

