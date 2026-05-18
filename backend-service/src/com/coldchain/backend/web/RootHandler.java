package com.coldchain.backend.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RootHandler implements HttpHandler {
    private final int port;

    public RootHandler(int port) {
        this.port = port;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/health".equals(path)) {
            writeJson(exchange, 200, "{\"status\":\"UP\",\"service\":\"backend-service\"}");
            return;
        }

        if ("/api-docs".equals(path)) {
            writeHtml(exchange, 200, buildApiDocsPage());
            return;
        }

        if ("/".equals(path)) {
            writeHtml(exchange, 200, buildIndexPage());
            return;
        }

        writeHtml(exchange, 404, buildNotFoundPage());
    }

    private String buildIndexPage() {
        return String.format("""
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>Cold Chain Backend Service</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 40px; color: #10212b; }
                    .card { max-width: 860px; padding: 28px; border: 1px solid #d7e3ea; border-radius: 16px; background: #f8fbfc; }
                    h1 { margin-top: 0; }
                    code { background: #eef4f7; padding: 2px 6px; border-radius: 6px; }
                    ul { line-height: 1.8; }
                    a { color: #0b66c3; text-decoration: none; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>冷链后端服务已启动</h1>
                    <p>当前服务端口：<code>%d</code></p>
                    <p>可直接访问下面这些地址进行验证：</p>
                    <ul>
                      <li><a href="/api-docs">/api-docs</a></li>
                      <li><a href="/health">/health</a></li>
                      <li><a href="/api/v1/vehicles">/api/v1/vehicles</a></li>
                      <li><a href="/api/v1/vehicles/CC-VA-01/telemetry/latest">/api/v1/vehicles/CC-VA-01/telemetry/latest</a></li>
                      <li><a href="/api/v1/vehicles/CC-VA-01/alerts?limit=1">/api/v1/vehicles/CC-VA-01/alerts?limit=1</a></li>
                    </ul>
                  </div>
                </body>
                </html>
                """, port);
    }

    private String buildApiDocsPage() {
        return String.format("""
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>接口文档</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 32px; color: #10212b; background: #f5f8fa; }
                    .wrap { max-width: 980px; margin: 0 auto; }
                    .card { background: #fff; border: 1px solid #d7e3ea; border-radius: 16px; padding: 24px; margin-bottom: 20px; }
                    h1, h2, h3 { margin-top: 0; }
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
                        <li><a href="/api/v1/vehicles/CC-VA-01/telemetry/latest">获取车辆最新温度数据</a></li>
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
                      <h2>1. 获取车辆列表</h2>
                      <p><code>GET /api/v1/vehicles</code></p>
                      <pre>{
  "success": true,
  "message": "ok",
  "data": [
    {
      "vehicleId": "CC-VA-01",
      "plateNumber": "京A-1024",
      "cargoType": "VACCINE",
      "cargoName": "疫苗",
      "safeTempMin": 2,
      "safeTempMax": 8,
      "status": "IN_TRANSIT"
    }
  ]
}</pre>
                    </div>

                    <div class="card">
                      <h2>2. 获取车辆最新温度数据</h2>
                      <p><code>GET /api/v1/vehicles/{vehicleId}/telemetry/latest</code></p>
                      <pre>{
  "success": true,
  "message": "ok",
  "data": {
    "vehicleId": "CC-VA-01",
    "recordTime": "2026-05-18 09:25:00",
    "temperature": 7.5,
    "humidity": 70,
    "doorOpen": false,
    "speed": 35,
    "outsideTemp": 31,
    "lng": 116.4,
    "lat": 39.91,
    "remainingKm": 13.4,
    "trend": "逼近上限"
  }
}</pre>
                    </div>

                    <div class="card">
                      <h2>3. 获取车辆告警记录</h2>
                      <p><code>GET /api/v1/vehicles/{vehicleId}/alerts?limit=1</code></p>
                      <pre>{
  "success": true,
  "message": "ok",
  "data": [
    {
      "alertId": "ALT-20260518-001",
      "vehicleId": "CC-VA-01",
      "level": "HIGH",
      "title": "高风险临界告警",
      "detail": "疫苗车厢温度接近安全上限，剩余路线较长。",
      "suggestion": "比较最近冷库改道方案与继续配送方案的综合成本。",
      "triggerTime": "2026-05-18 09:25:00"
    }
  ]
}</pre>
                    </div>
                  </div>
                </body>
                </html>
                """, port);
    }

    private String buildNotFoundPage() {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8" />
                  <title>404 Not Found</title>
                </head>
                <body>
                  <h1>404 Not Found</h1>
                  <p>请访问 <a href="/">首页</a> 查看可用接口。</p>
                </body>
                </html>
                """;
    }

    private void writeHtml(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private void writeJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
