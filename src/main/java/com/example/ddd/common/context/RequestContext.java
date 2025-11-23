package com.example.ddd.common.context;

import lombok.Getter;
import lombok.Setter;

/**
 * 请求上下文
 * 存储当前请求的相关信息（IP、User-Agent 等）
 * 使用 ThreadLocal 确保线程安全
 */
@Getter
@Setter
public class RequestContext {
    
    /**
     * 客户端 IP 地址
     */
    private String clientIp;
    
    /**
     * User-Agent
     */
    private String userAgent;
    
    /**
     * 请求 URI
     */
    private String requestUri;
    
    /**
     * 请求方法
     */
    private String requestMethod;
    
    /**
     * 请求开始时间（毫秒时间戳）
     */
    private Long requestStartTime;
    
    /**
     * 请求 ID（用于日志追踪）
     */
    private String requestId;
    
    /**
     * ThreadLocal 存储当前请求的上下文
     */
    private static final ThreadLocal<RequestContext> CONTEXT_HOLDER = new ThreadLocal<>();
    
    /**
     * 获取当前请求上下文
     */
    public static RequestContext getCurrentContext() {
        return CONTEXT_HOLDER.get();
    }
    
    /**
     * 设置当前请求上下文
     */
    public static void setCurrentContext(RequestContext context) {
        CONTEXT_HOLDER.set(context);
    }
    
    /**
     * 清除当前请求上下文
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
    
    /**
     * 获取客户端 IP（优先从 X-Forwarded-For 获取，适用于反向代理场景）
     */
    public static String extractClientIp(String xForwardedFor, String xRealIp, String remoteAddr) {
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            String[] ips = xForwardedFor.split(",");
            if (ips.length > 0) {
                return ips[0].trim();
            }
        }
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}



