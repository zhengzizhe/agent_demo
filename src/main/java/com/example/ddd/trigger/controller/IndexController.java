package com.example.ddd.trigger.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 首页控制器
 * 服务前端页面
 */
@Controller
public class IndexController {

    @Get("/")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse<String> index() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("static/index.html");
            if (inputStream != null) {
                String html = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                return HttpResponse.ok(html);
            }
        } catch (Exception e) {
            // 忽略错误，返回默认页面
        }
        return HttpResponse.ok("<html><body><h1>Agent Demo</h1><p>请访问 /static/index.html</p></body></html>");
    }
}

