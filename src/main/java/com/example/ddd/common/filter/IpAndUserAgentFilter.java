package com.example.ddd.common.filter;

import com.example.ddd.common.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * IP 和 User-Agent 提取过滤器
 * 在 JWT 过滤器之前执行，用于提取和存储请求相关信息（IP、User-Agent 等）
 */
@Slf4j
@Component
@Order(1) // 确保在 JWT 过滤器之前执行
public class IpAndUserAgentFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // 创建请求上下文
            RequestContext context = new RequestContext();
            // 提取客户端 IP
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            String xRealIp = request.getHeader("X-Real-IP");
            String remoteAddr = request.getRemoteAddr();
            String clientIp = RequestContext.extractClientIp(xForwardedFor, xRealIp, remoteAddr);
            context.setClientIp(clientIp);
            String userAgent = request.getHeader("User-Agent");
            context.setUserAgent(userAgent != null ? userAgent : "unknown");
            // 提取请求 URI 和方法
            context.setRequestUri(request.getRequestURI());
            context.setRequestMethod(request.getMethod());
            // 设置请求开始时间
            context.setRequestStartTime(System.currentTimeMillis());
            // 生成请求 ID（用于日志追踪）
            context.setRequestId(UUID.randomUUID().toString().replace("-", ""));
            // 设置到 ThreadLocal
            RequestContext.setCurrentContext(context);
            // 记录请求信息（可选，用于调试）
            log.debug("请求上下文已设置: requestId={}, ip={}, uri={}, method={}, userAgent={}",
                    context.getRequestId(), context.getClientIp(), context.getRequestUri(),
                    context.getRequestMethod(), context.getUserAgent());
            // 继续过滤器链
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清除上下文，防止内存泄漏
            RequestContext.clear();
        }
    }
}

