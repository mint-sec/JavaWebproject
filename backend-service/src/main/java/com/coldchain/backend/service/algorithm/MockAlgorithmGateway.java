package com.coldchain.backend.service.algorithm;

import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.TelemetryRecord;
import com.coldchain.backend.entity.Vehicle;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MockAlgorithmGateway implements AlgorithmGateway {
    private final String algorithmVersion;

    public MockAlgorithmGateway(@Value("${app.algorithm.version:mock-risk-v1}") String algorithmVersion) {
        this.algorithmVersion = algorithmVersion;
    }

    @Override
    public AlgorithmEvaluation evaluate(
            String vehicleCode,
            Vehicle vehicle,
            TelemetryRecord latestTelemetry,
            List<TelemetryRecord> telemetryHistory,
            List<AlertRecord> alerts) {
        double temperature = latestTelemetry.temperature();
        int alertCount = alerts.size();
        boolean anomalyDetected = temperature >= 6.5 || alertCount >= 2;

        double riskScore = calculateRiskScore(vehicle, latestTelemetry, alertCount);
        String riskLevel = mapRiskLevel(riskScore);
        String riskLabel = mapRiskLabel(riskLevel);
        Integer predictedMinutes = temperature >= 7.0 ? 12 : temperature >= 6.0 ? 20 : null;

        List<AlgorithmRecommendation> recommendations = buildRecommendations(riskLevel, latestTelemetry);
        String anomalyReason = anomalyDetected
                ? "连续升温且已触发多条预警，存在温控失稳风险"
                : "当前温控处于可控范围内";

        return new AlgorithmEvaluation(
                anomalyDetected,
                anomalyDetected ? "TREND_RISE" : "NORMAL",
                anomalyReason,
                predictedMinutes,
                riskScore,
                riskLevel,
                riskLabel,
                algorithmVersion,
                "MOCK_GATEWAY",
                recommendations);
    }

    private double calculateRiskScore(Vehicle vehicle, TelemetryRecord latestTelemetry, int alertCount) {
        double deviationScore = Math.max(0, latestTelemetry.temperature() - vehicle.safeTempMin()) * 8;
        double distanceScore = Math.min(latestTelemetry.remainingKm(), 30) * 1.2;
        double doorScore = latestTelemetry.doorOpen() ? 12 : 0;
        double alertScore = alertCount * 8;
        return Math.min(98.0, deviationScore + distanceScore + doorScore + alertScore);
    }

    private String mapRiskLevel(double riskScore) {
        if (riskScore >= 80) {
            return "HIGH";
        }
        if (riskScore >= 45) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String mapRiskLabel(String riskLevel) {
        return switch (riskLevel) {
            case "HIGH" -> "高风险";
            case "MEDIUM" -> "中风险";
            default -> "低风险";
        };
    }

    private List<AlgorithmRecommendation> buildRecommendations(String riskLevel, TelemetryRecord latestTelemetry) {
        List<AlgorithmRecommendation> recommendations = new ArrayList<>();
        if ("HIGH".equals(riskLevel)) {
            recommendations.add(new AlgorithmRecommendation(
                    "REROUTE_COLD_STORAGE",
                    "改道最近冷库",
                    "优先前往最近冷库进行临时控温，降低货损风险。",
                    "增加 8 分钟路程成本",
                    "预计 3 公里内恢复控温",
                    true));
            recommendations.add(new AlgorithmRecommendation(
                    "PRIORITY_DELIVERY",
                    "优先配送最近医院",
                    "缩短高敏货物暴露时间，减少超温影响。",
                    "需调整后续配送顺序",
                    "可减少约 18 分钟暴露时间",
                    false));
        } else if ("MEDIUM".equals(riskLevel)) {
            recommendations.add(new AlgorithmRecommendation(
                    "PRIORITY_DELIVERY",
                    "优先配送最近站点",
                    "继续当前配送，但优先完成最近高敏站点。",
                    "整体路线小幅调整",
                    "缩短剩余暴露时间",
                    true));
            recommendations.add(new AlgorithmRecommendation(
                    "CHECK_REFRIGERATION",
                    "检查制冷与车门状态",
                    "建议司机确认车门关闭状态并检查制冷设备工作情况。",
                    "无需新增路线成本",
                    "避免风险进一步升高",
                    false));
        } else {
            recommendations.add(new AlgorithmRecommendation(
                    "FOLLOW_CURRENT_ROUTE",
                    "按原计划配送",
                    "当前温控风险较低，继续按原计划执行。",
                    "无新增成本",
                    "运输效率最优",
                    true));
        }
        return recommendations;
    }
}
