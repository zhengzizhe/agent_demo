package com.example.ddd.application.auth.service.login;

import com.example.ddd.domain.user.LoginType;
import com.example.ddd.interfaces.request.LoginRequest;

public interface LoginMethod {
    public void login(LoginRequest request);

    public LoginType type();
}
