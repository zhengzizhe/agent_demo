package com.example.ddd.infrastructure.dao.po;

import com.example.ddd.domain.user.emailVerification.EmailVerification;
import com.example.ddd.domain.user.emailVerification.VerificationPurpose;
import com.example.ddd.domain.user.emailVerification.VerificationStatus;
import com.example.ddd.domain.user.valueobj.Email;
import com.example.ddd.domain.user.valueobj.VerificationCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 邮箱验证码持久化对象
 * 对应表：email_verification
 */
@Getter
@Setter
public class EmailVerificationPO {
    /**
     * 雪花ID，主键
     */
    private Long id;

    /**
     * 接收验证码的邮箱
     */
    private String email;

    /**
     * 验证码内容
     */
    private String code;

    /**
     * 验证码用途：register / login / reset_password / bind_email 等
     */
    private String purpose;

    /**
     * 验证码状态：0未使用，1已使用，2已失效
     */
    private Integer status;

    /**
     * 验证码过期时间（毫秒时间戳）
     */
    private Long expireAt;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 使用时间（毫秒时间戳）
     */
    private Long usedAt;

    /**
     * 请求验证码的客户端 IP
     */
    private String clientIp;

    /**
     * 请求时的 UA 信息
     */
    private String userAgent;

    /**
     * 转换为领域对象 EmailVerification
     *
     * @return EmailVerification 领域对象
     */
    public EmailVerification toDomain() {
        if (this.email == null || this.email.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        EmailVerification verification = new EmailVerification();
        verification.setId(this.id);

        // 设置邮箱
        verification.setEmail(new Email(this.email));

        // 设置验证码
        if (this.code != null && !this.code.isEmpty()) {
            verification.setCode(new VerificationCode(this.code));
        }

        // 转换用途
        if (this.purpose != null) {
            VerificationPurpose purpose = VerificationPurpose.fromValue(this.purpose);
            verification.setPurpose(purpose != null ? purpose : VerificationPurpose.REGISTER);
        } else {
            verification.setPurpose(VerificationPurpose.REGISTER);
        }

        // 转换状态
        if (this.status != null) {
            VerificationStatus status = VerificationStatus.fromCode(this.status);
            verification.setStatus(status != null ? status : VerificationStatus.UNUSED);
        } else {
            verification.setStatus(VerificationStatus.UNUSED);
        }

        // 设置时间戳
        verification.setExpireAt(this.expireAt != null ? this.expireAt : 0);
        verification.setCreatedAt(this.createdAt != null ? this.createdAt : 0);
        verification.setUsedAt(this.usedAt);

        // 设置请求信息
        verification.setClientIp(this.clientIp);
        verification.setUserAgent(this.userAgent);

        return verification;
    }

    /**
     * 从领域对象 EmailVerification 创建 PO
     *
     * @param verification 邮箱验证码领域对象
     * @return EmailVerificationPO 持久化对象
     */
    public static EmailVerificationPO fromDomain(EmailVerification verification) {
        if (verification == null) {
            return null;
        }

        EmailVerificationPO po = new EmailVerificationPO();
        po.setId(verification.getId());

        // 设置邮箱
        if (verification.getEmail() != null) {
            po.setEmail(verification.getEmail().getValue());
        }

        // 设置验证码
        if (verification.getCode() != null) {
            po.setCode(verification.getCode().getValue());
        }

        // 转换用途
        if (verification.getPurpose() != null) {
            po.setPurpose(verification.getPurpose().getValue());
        } else {
            po.setPurpose(VerificationPurpose.REGISTER.getValue());
        }

        // 转换状态
        if (verification.getStatus() != null) {
            po.setStatus(verification.getStatus().getCode());
        } else {
            po.setStatus(VerificationStatus.UNUSED.getCode());
        }

        // 设置时间戳
        po.setExpireAt(verification.getExpireAt());
        po.setCreatedAt(verification.getCreatedAt());
        po.setUsedAt(verification.getUsedAt());

        // 设置请求信息
        po.setClientIp(verification.getClientIp());
        po.setUserAgent(verification.getUserAgent());

        return po;
    }
}

