package com.example.ddd.application.auth.service.login.impl;

import com.example.ddd.application.auth.service.login.LoginMethod;
import com.example.ddd.domain.user.LoginType;
import com.example.ddd.interfaces.request.LoginRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GithubMethod implements LoginMethod {
    @Override
    public void login(LoginRequest request) {
        log.info("{}{}登录:{}", request.getProvider(), type(), request.getAuthCode());


    }

    @Override
    public LoginType type() {
        return LoginType.Github;
    }
}
