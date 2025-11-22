package com.example.ddd.interfaces.controller;

import com.example.ddd.application.auth.service.AuthService;
import com.example.ddd.interfaces.request.LoginRequest;
import com.example.ddd.interfaces.request.SendVerificationCodeRequest;
import com.example.ddd.interfaces.response.LoginResponse;
import com.example.ddd.interfaces.response.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供登录、注册、验证码等认证相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    AuthService authService;

    /**
     * 用户登录接口
     * <p>
     * 支持多种登录方式：
     * 1. 邮箱密码登录：loginType=email, email + password
     * 2. 邮箱验证码登录：loginType=email_code, email + code
     * 3. OAuth 登录：loginType=oauth, provider + authCode + state
     *
     * @param request 登录请求
     * @return 登录响应（包含 JWT Token 和用户信息）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: loginType={}, email={}", request.getLoginType(), request.getEmail());
        // TODO: 实现登录逻辑
        // 1. 根据 loginType 判断登录方式
        // 2. 验证用户凭证（密码/验证码/OAuth）
        // 3. 查询用户信息
        // 4. 生成 JWT Token
        // 5. 记录登录日志
        // 6. 更新用户最后登录时间
        // 示例响应（实际应从业务逻辑获取）
        LoginResponse response = LoginResponse.builder()
                .token("example-jwt-token")
                .tokenType("Bearer")
                .expiresAt(System.currentTimeMillis() + 86400000L) // 24小时后过期
                .userUuid("example-uuid")
                .email(request.getEmail())
                .displayName("示例用户")
                .isFirstLogin(false)
                .build();

        return Result.success("登录成功", response);
    }

    /**
     * 发送邮箱验证码接口
     *
     * @param request 发送验证码请求
     * @return 发送结果
     */
    @PostMapping("/verification-code/send")
    public Result<String> sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request) {
        log.info("发送验证码请求: email={}, purpose={}", request.getEmail(), request.getPurpose());
        authService.sendVerificationCode(request);
        return Result.success("验证码已发送，请查收邮箱");
    }

    /**
     * 用户注册接口
     *
     * @param request 注册请求（包含邮箱、验证码、密码等）
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody LoginRequest request) {
        log.info("用户注册请求: email={}", request.getEmail());
        // TODO: 实现注册逻辑
        // 1. 验证邮箱格式
        // 2. 验证邮箱是否已注册
        // 3. 验证验证码（purpose=register）
        // 4. 创建用户账户
        // 5. 创建认证身份记录
        // 6. 生成 JWT Token
        // 7. 记录登录日志

        // 示例响应
        LoginResponse response = LoginResponse.builder()
                .token("example-jwt-token")
                .tokenType("Bearer")
                .expiresAt(System.currentTimeMillis() + 86400000L)
                .userUuid("example-uuid")
                .email(request.getEmail())
                .displayName("新用户")
                .isFirstLogin(true)
                .build();

        return Result.success("注册成功", response);
    }

    /**
     * 刷新 Token 接口
     *
     * @param token 当前 Token（从请求头获取）
     * @return 新的 Token
     */
    @PostMapping("/refresh-token")
    public Result<LoginResponse> refreshToken(@RequestHeader("Authorization") String token) {
        log.info("刷新 Token 请求");

        // TODO: 实现刷新 Token 逻辑
        // 1. 从请求头提取 Token（去掉 "Bearer " 前缀）
        // 2. 验证 Token 是否有效
        // 3. 检查 Token 是否即将过期（例如：剩余时间少于30分钟）
        // 4. 生成新的 Token
        // 5. 返回新 Token

        // 示例响应
        LoginResponse response = LoginResponse.builder()
                .token("new-jwt-token")
                .tokenType("Bearer")
                .expiresAt(System.currentTimeMillis() + 86400000L)
                .build();

        return Result.success("Token 刷新成功", response);
    }

    /**
     * 用户登出接口
     *
     * @param token 当前 Token（从请求头获取）
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String token) {
        log.info("用户登出请求");

        // TODO: 实现登出逻辑
        // 1. 从请求头提取 Token
        // 2. 将 Token 加入黑名单（可选，使用 Redis 存储）
        // 3. 记录登出日志

        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息接口
     *
     * @param token 当前 Token（从请求头获取）
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<LoginResponse> getCurrentUser(@RequestHeader("Authorization") String token) {
        log.info("获取当前用户信息请求");

        // TODO: 实现获取用户信息逻辑
        // 1. 从请求头提取 Token
        // 2. 验证 Token 是否有效
        // 3. 从 Token 中提取用户ID
        // 4. 查询用户信息
        // 5. 返回用户信息

        // 示例响应
        LoginResponse response = LoginResponse.builder()
                .userUuid("example-uuid")
                .email("user@example.com")
                .displayName("示例用户")
                .build();

        return Result.success(response);
    }
}

