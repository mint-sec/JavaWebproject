package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import com.coldchain.algorithm.model.EvaluateResponse.RecommendationPayload;
import java.util.ArrayList;
import java.util.List;

/**
 * 路径建议模块（第 2 周优化版）
 *
 * 不只看风险等级，还结合剩余路程、车门状态、外部温度综合判断：
 *
 * | 风险 | 剩余公里 | 车门 | 外部温度 | 推荐方案 |
 * |------|---------|------|---------|---------|
 * | HIGH | ≤5      | —    | —       | 加速配送（快到了，别绕路） |
 * | HIGH | >5      | —    | —       | 改道冷库 + 优先配送备选 |
 * | MED  | ≤8      | —    | —       | 优先配送最近站点 |
 * | MED  | >8      | 开   | >30     | 关闭车门+检查制冷 → 优先配送 |
 * | MED  | >8      | —    | —       | 优先配送 + 检查制冷备选 |
 * | LOW  | —       | 开   | >30     | 关闭车门 |
 * | LOW  | —       | —    | >35     | 按原计划 + 提醒制冷负载 |
 * | LOW  | —       | —    | —       | 按原计划 |
 */
public class RoutePlanner {

    private static final double NEAR_DESTINATION = 5.0;  // "快到了"阈值
    private static final double MEDIUM_NEAR = 8.0;

    public List<RecommendationPayload> generate(
            String riskLevel, EvaluateRequest.TelemetryPayload t) {

        double remainingKm = t.getRemainingKm();
        boolean doorOpen   = t.isDoorOpen();
        double outsideTemp = t.getOutsideTemp();

        List<RecommendationPayload> list = new ArrayList<>();

        switch (riskLevel) {
            // ==================== HIGH ====================
            case "HIGH" -> {
                if (remainingKm <= NEAR_DESTINATION) {
                    list.add(make("PRIORITY_DELIVERY",
                            "加速完成配送",
                            "终点仅剩" + String.format("%.1f", remainingKm) + "公里，加速赶到后可立即处理货损。",
                            "无新增路线成本",
                            "最快抵达终点，减少暴露时间",
                            true));
                } else {
                    list.add(make("REROUTE_COLD_STORAGE",
                            "改道最近冷库",
                            "优先前往最近冷库进行临时控温，降低货损风险。",
                            "增加8分钟路程成本",
                            "预计3公里内恢复控温",
                            true));
                    list.add(make("PRIORITY_DELIVERY",
                            "优先配送最近医院",
                            "缩短高敏货物暴露时间，减少超温影响。",
                            "需调整后续配送顺序",
                            "可减少约18分钟暴露时间",
                            false));
                }
            }

            // ==================== MEDIUM ====================
            case "MEDIUM" -> {
                if (remainingKm <= MEDIUM_NEAR) {
                    list.add(make("PRIORITY_DELIVERY",
                            "优先配送最近站点",
                            "距离较近（" + String.format("%.1f", remainingKm) + "km），优先完成配送。",
                            "小幅调整配送顺序",
                            "减少在途暴露时间",
                            true));
                } else if (doorOpen && outsideTemp > 30) {
                    list.add(make("CHECK_REFRIGERATION",
                            "关闭车门并检查制冷",
                            "当前车门开启且外部" + String.format("%.0f", outsideTemp) + "°C，热量持续灌入，先关门再继续。",
                            "需临时靠边停车",
                            "立即阻断热源",
                            true));
                    list.add(make("PRIORITY_DELIVERY",
                            "优先配送最近站点",
                            "确认车门关闭后，优先完成最近配送。",
                            "调整顺序",
                            "减少剩余暴露时间",
                            false));
                } else {
                    list.add(make("PRIORITY_DELIVERY",
                            "优先配送最近站点",
                            "加快配送节奏，缩短货物在途暴露时间。",
                            "需调整后续配送顺序",
                            "可减少约10分钟暴露时间",
                            true));
                    list.add(make("CHECK_REFRIGERATION",
                            "检查制冷与车门",
                            "确认制冷设备运行正常，排查车门密封情况。",
                            "需临时靠边停车检查",
                            "可及时发现设备隐患",
                            false));
                }
            }

            // ==================== LOW ====================
            default -> {
                if (doorOpen && outsideTemp > 30) {
                    list.add(make("CHECK_REFRIGERATION",
                            "关闭车门继续配送",
                            "车门开启，当前外部" + String.format("%.0f", outsideTemp) + "°C，请关闭车门后继续。",
                            "无新增路线成本",
                            "防止热空气灌入",
                            true));
                } else if (outsideTemp > 35) {
                    list.add(make("FOLLOW_CURRENT_ROUTE",
                            "按原计划配送（注意制冷负载）",
                            "当前温控正常，但外部" + String.format("%.0f", outsideTemp) + "°C极高温，制冷设备负荷较大。",
                            "无",
                            "无",
                            true));
                } else {
                    list.add(make("FOLLOW_CURRENT_ROUTE",
                            "按原计划配送",
                            "当前温度正常，无需调整路线。",
                            "无",
                            "无",
                            true));
                }
            }
        }
        return list;
    }

    private RecommendationPayload make(String type, String title, String detail,
                                        String cost, String benefit, boolean rec) {
        RecommendationPayload r = new RecommendationPayload();
        r.setPlanType(type);
        r.setTitle(title);
        r.setDetail(detail);
        r.setEstimatedCost(cost);
        r.setEstimatedBenefit(benefit);
        r.setRecommended(rec);
        return r;
    }
}
