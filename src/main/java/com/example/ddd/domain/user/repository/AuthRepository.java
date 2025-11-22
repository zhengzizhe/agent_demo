package com.example.ddd.domain.user.repository;

import com.example.ddd.application.auth.repository.IAuthRepository;
import com.example.ddd.domain.user.emailVerification.EmailVerification;
import com.example.ddd.infrastructure.dao.mapper.EmailVerificationMapper;
import com.example.ddd.infrastructure.dao.po.EmailVerificationPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AuthRepository implements IAuthRepository {
    @Resource
    EmailVerificationMapper emailVerificationMapper;

    @Override
    public void save(EmailVerification newVerification) {
        EmailVerificationPO emailVerificationPO = EmailVerificationPO.fromDomain(newVerification);
        emailVerificationMapper.insert(emailVerificationPO);
    }
}
