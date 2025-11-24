package com.example.ddd.application.auth.service;

import com.example.ddd.application.auth.service.command.AuthCommandHandler;
import com.example.ddd.application.auth.service.query.AuthQueryHandler;
import com.example.ddd.domain.user.User;
import com.example.ddd.interfaces.request.LoginRequest;
import com.example.ddd.interfaces.request.SendVerificationCodeRequest;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Resource
    AuthCommandHandler commandHandler;
    @Resource
    AuthQueryHandler queryHandler;

    public void sendVerificationCode(@Valid SendVerificationCodeRequest request) {
        commandHandler.verification(request);
    }

    public User login(@Valid LoginRequest request) {
        commandHandler.login(request);
        return null;
    }
}
