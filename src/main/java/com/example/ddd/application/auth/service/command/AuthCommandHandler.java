package com.example.ddd.application.auth.service.command;

import com.example.ddd.application.auth.repository.IAuthRepository;
import com.example.ddd.common.context.RequestContext;
import com.example.ddd.domain.user.emailVerification.EmailVerification;
import com.example.ddd.domain.user.emailVerification.EmailVerificationFactory;
import com.example.ddd.domain.user.valueobj.Email;
import com.example.ddd.interfaces.request.SendVerificationCodeRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AuthCommandHandler {
    @Resource
    IAuthRepository IAuthRepository;
    @Resource
    EmailVerificationFactory emailVerificationFactory;

    @Resource
    public void verification(SendVerificationCodeRequest request) {
        String code = "123456";
        // TODO: 实现发送验证码逻辑
        EmailVerification newVerification = emailVerificationFactory.createNewVerification(new Email(request.getEmail()), request.getPurpose(), RequestContext.getCurrentContext().getClientIp(), RequestContext.getCurrentContext().getUserAgent());
        IAuthRepository.save(newVerification);
        // 6. 记录发送日志

    }
}
