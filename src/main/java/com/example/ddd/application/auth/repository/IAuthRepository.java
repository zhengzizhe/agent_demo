package com.example.ddd.application.auth.repository;


import com.example.ddd.domain.user.emailVerification.EmailVerification;

public interface IAuthRepository {
    void save(EmailVerification newVerification);
}
