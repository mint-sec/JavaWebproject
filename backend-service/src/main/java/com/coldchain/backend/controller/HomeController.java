package com.coldchain.backend.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @Value("${server.port}")
    private int port;

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() {
        return ResponseEntity.ok(buildIndexPage());
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "backend-service",
                "port", port);
    }

    @GetMapping(value = "/api-docs", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> apiDocs() {
        return ResponseEntity.ok(buildApiDocsPage());
    }

    private String buildIndexPage() {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>Cold Chain Backend Service</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 40px; color: #10212b; background: #f7fbfc; }
                    .card { max-width: 880px; padding: 28px; border: 1px solid #d7e3ea; border-radius: 16px; background: #ffffff; }
                    h1 { margin-top: 0; }
                    code { background: #eef4f7; padding: 2px 6px; border-radius: 6px; }
                    ul { line-height: 1.9; }
                    a { color: #0b66c3; text-decoration: none; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>冷链后端服务已启动</h1>
                    <p>当前服务端口：<code>%d</code></p>
                    <p>这是一套按 PRD 推进的 Spring Boot 后端服务，当前可直接访问：</p>
                    <ul>
                      <li><a href="/api-docs">/api-docs</a></li>
                      <li><a href="/health">/health</a></li>
                      <li><a href="/api/v1/vehicles">/api/v1/vehicles</a></li>
                      <li><a href="/api/v1/dashboard/vehicles/CC-VA-01">/api/v1/dashboard/vehicles/CC-VA-01</a></li>
                      <li><a href="/api/v1/vehicles/CC-VA-01/telemetry/latest">/api/v1/vehicles/CC-VA-01/telemetry/latest</a></li>
                      <li><a href="/api/v1/vehicles/CC-VA-01/telemetry/history?minutes=30">/api/v1/vehicles/CC-VA-01/telemetry/history?minutes=30</a></li>
                      <li><a href="/api/v1/vehicles/CC-VA-01/alerts?limit=2">/api/v1/vehicles/CC-VA-01/alerts?limit=2</a></li>
                    </ul>
                  </div>
                </body>
                </html>
                """.formatted(port);
    }

    private String buildApiDocsPage() {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>接口文档</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 32px; color: #10212b; background: #f5f8fa; }
                    .wrap { max-width: 1020px; margin: 0 auto; }
                    .card { background: #fff; border: 1px solid #d7e3ea; border-radius: 16px; padding: 24px; margin-bottom: 20px; }
                    h1, h2 { margin-top: 0; }
                    code { background: #eef4f7; padding: 2px 6px; border-radius: 6px; }
                    pre { background: #0f1d24; color: #eaf3f7; padding: 16px; border-radius: 12px; overflow-x: auto; }
                    ul { line-height: 1.8; }
                    a { color: #0b66c3; text-decoration: none; }
                    .muted { color: #54717d; }
                  </style>
                </head>
                <body>
                  <div class="wrap">
                    <div class="card">
                      <h1>后端接口文档</h1>
                      <p class="muted">服务地址：<code>http://localhost:%d</code></p>
                      <p>接口前缀：<code>/api/v1</code></p>
                      <ul>
                        <li><a href="/">返回首页</a></li>
                        <li><a href="/health">健康检查</a></li>
                        <li><a href="/api/v1/vehicles">获取车辆列表</a></li>
                        <li><a href="/api/v1/dashboard/vehicles/CC-VA-01">获取大屏聚合数据</a></li>
                        <li><a href="/api/v1/vehicles/CC-VA-01/telemetry/latest">获取车辆最新温度数据</a></li>
                        <li><a href="/api/v1/vehicles/CC-VA-01/telemetry/history?minutes=30">获取历史温度数据</a></li>
                        <li><a href="/api/v1/vehicles/CC-VA-01/alerts?limit=1">获取车辆告警记录</a></li>
                      </ul>
                    </div>
                    <div class="card">
                      <h2>通用返回结构</h2>
                      <pre>{
  "success": true,
  "message": "ok",
  "data": {}
}</pre>
                    </div>
                    <div class="card">
                      <h2>已实现接口</h2>
                      <ul>
                        <li><code>GET /api/v1/vehicles</code></li>
                        <li><code>GET /api/v1/dashboard/vehicles/{vehicleCode}</code></li>
                        <li><code>GET /api/v1/vehicles/{vehicleCode}/telemetry/latest</code></li>
                        <li><code>GET /api/v1/vehicles/{vehicleCode}/telemetry/history?minutes=30</code></li>
                        <li><code>GET /api/v1/vehicles/{vehicleCode}/alerts?limit=4</code></li>
                        <li><code>GET /api/v1/alerts?vehicleCode=CC-VA-01&page=1&pageSize=20</code></li>
                        <li><code>GET /api/v1/alerts/{alertId}</code></li>
                      </ul>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(port);
    }
}
