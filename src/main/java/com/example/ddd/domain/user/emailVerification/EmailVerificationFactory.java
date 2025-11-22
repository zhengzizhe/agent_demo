package com.example.ddd.domain.user.emailVerification;

import com.example.ddd.common.utils.IdGenerator;
import com.example.ddd.domain.user.valueobj.Email;
import com.example.ddd.domain.user.valueobj.VerificationCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * 邮箱验证码工厂类
 * 负责创建 EmailVerification 聚合根对象
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationFactory {

    private final IdGenerator idGenerator;

    /**
     * 验证码默认有效期（毫秒）：10分钟
     */
    private static final long DEFAULT_EXPIRATION_TIME = 10 * 60 * 1000L;


    /**
     * 创建新的邮箱验证码（用于发送验证码场景）
     * 自动生成 ID、设置默认状态为未使用、设置过期时间
     *
     * @param email    邮箱
     * @param code     验证码（由外部传入）
     * @param purpose  验证码用途
     * @param clientIp 客户端IP
     * @param userAgent User-Agent
     * @return EmailVerification 对象
     */
    public EmailVerification createNewVerification(
            Email email,
            String code,
            VerificationPurpose purpose,
            String clientIp,
            String userAgent) {
        return createNewVerification(email, code, purpose, clientIp, userAgent, DEFAULT_EXPIRATION_TIME);
    }

    /**
     * 创建新的邮箱验证码（自定义过期时间）
     *
     * @param email        邮箱
     * @param code         验证码（由外部传入）
     * @param purpose      验证码用途
     * @param clientIp     客户端IP
     * @param userAgent    User-Agent
     * @param expirationMs 过期时间（毫秒）
     * @return EmailVerification 对象
     */
    public EmailVerification createNewVerification(
            Email email,
            String code,
            VerificationPurpose purpose,
            String clientIp,
            String userAgent,
            long expirationMs) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("验证码不能为空");
        }

        long id = idGenerator.nextSnowflakeId();
        long now = System.currentTimeMillis();
        long expireAt = now + expirationMs;

        EmailVerification verification = new EmailVerification();
        verification.setId(id);
        verification.setEmail(email);
        verification.setCode(new VerificationCode(code));
        verification.setPurpose(purpose);
        verification.setStatus(VerificationStatus.UNUSED);
        verification.setExpireAt(expireAt);
        verification.setCreatedAt(now);
        verification.setUsedAt(null);
        verification.setClientIp(clientIp);
        verification.setUserAgent(userAgent != null ? userAgent : "unknown");

        return verification;
    }

    /**
     * 从数据库重建验证码对象（用于查询场景）
     *
     * @param id        验证码ID
     * @param email     邮箱
     * @param code      验证码
     * @param purpose   用途
     * @param status    状态
     * @param expireAt  过期时间
     * @param createdAt 创建时间
     * @param usedAt    使用时间
     * @param clientIp  客户端IP
     * @param userAgent User-Agent
     * @return EmailVerification 对象
     */
    public EmailVerification reconstructVerification(
            Long id,
            Email email,
            String code,
            VerificationPurpose purpose,
            VerificationStatus status,
            long expireAt,
            long createdAt,
            Long usedAt,
            String clientIp,
            String userAgent) {
        EmailVerification verification = new EmailVerification();
        verification.setId(id);
        verification.setEmail(email);
        verification.setCode(new VerificationCode(code));
        verification.setPurpose(purpose);
        verification.setStatus(status);
        verification.setExpireAt(expireAt);
        verification.setCreatedAt(createdAt);
        verification.setUsedAt(usedAt);
        verification.setClientIp(clientIp);
        verification.setUserAgent(userAgent);

        return verification;
    }

}

