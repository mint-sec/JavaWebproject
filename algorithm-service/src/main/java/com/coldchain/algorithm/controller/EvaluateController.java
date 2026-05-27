package com.coldchain.algorithm.controller;

import com.coldchain.algorithm.model.EvaluateRequest;
import com.coldchain.algorithm.model.EvaluateResponse;
import com.coldchain.algorithm.service.AnomalyDetector;
import com.coldchain.algorithm.service.AnomalyResult;
import com.coldchain.algorithm.service.RiskResult;
import com.coldchain.algorithm.service.RiskScorer;
import com.coldchain.algorithm.service.RoutePlanner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 算法服务 HTTP 入口 — POST /evaluate
 *
 * 对应成员B 的 HttpAlgorithmGateway 发来的请求
 * 接收 AlgorithmEvaluateRequest 格式的 JSON，返回 AlgorithmHttpResponse 格式的 JSON
 */
@RestController
public class EvaluateController {

    private final AnomalyDetector anomalyDetector = new AnomalyDetector();
    private final RiskScorer riskScorer = new RiskScorer();
    private final RoutePlanner routePlanner = new RoutePlanner();

    @PostMapping("/evaluate")
    public EvaluateResponse evaluate(@RequestBody EvaluateRequest request) {
        // 1. 异常检测
        AnomalyResult anomaly = anomalyDetector.detect(request);

        // 2. 风险评分（传入异常结果做加成）
        RiskResult risk = riskScorer.calculate(request, anomaly);

        // 3. 路径建议（传入遥测数据做场景细化）
        var recommendations = routePlanner.generate(risk.getLevel(), request.getLatestTelemetry());

        // 4. 组装响应
        EvaluateResponse response = new EvaluateResponse();
        response.setAnomalyDetected(anomaly.isDetected());
        response.setAnomalyType(anomaly.getType());
        response.setAnomalyReason(anomaly.getReason());
        response.setPredictedMinutesToLimit(anomaly.getMinutesToLimit());
        response.setRiskScore(risk.getScore());
        response.setRiskLevel(risk.getLevel());
        response.setRiskLabel(risk.getLabel());
        response.setAlgorithmVersion("risk-v2");
        response.setAlgorithmSource("JAVA_HTTP");
        response.setRecommendations(recommendations);

        return response;
    }
}
