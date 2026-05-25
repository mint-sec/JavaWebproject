package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateResponse.RecommendationPayload;
import java.util.ArrayList;
import java.util.List;

/**
 * 路径建议模块 — 根据风险等级生成调度建议
 *
 * 规则：
 *   HIGH   → 推荐改道冷库 + 备选优先配送
 *   MEDIUM → 推荐优先配送 + 备选检查制冷
 *   LOW    → 按原计划
 */
public class RoutePlanner {

    /**
     * 根据风险等级生成建议列表（1-3 条）
     */
    public List<RecommendationPayload> generate(String riskLevel) {
        List<RecommendationPayload> list = new ArrayList<>();

        switch (riskLevel) {
            case "HIGH" -> {
                // 推荐：改道冷库
                list.add(makePlan("REROUTE_COLD_STORAGE",
                        "改道最近冷库",
                        "优先前往最近冷库进行临时控温，降低货损风险。",
                        "增加8分钟路程成本",
                        "预计3公里内恢复控温",
                        true));
                // 备选：优先配送
                list.add(makePlan("PRIORITY_DELIVERY",
                        "优先配送最近医院",
                        "缩短高敏货物暴露时间，减少超温影响。",
                        "需调整后续配送顺序",
                        "可减少约18分钟暴露时间",
                        false));
            }
            case "MEDIUM" -> {
                // 推荐：优先配送
                list.add(makePlan("PRIORITY_DELIVERY",
                        "优先配送最近站点",
                        "加快配送节奏，缩短货物在途暴露时间。",
                        "需调整后续配送顺序",
                        "可减少约10分钟暴露时间",
                        true));
                // 备选：检查制冷
                list.add(makePlan("CHECK_REFRIGERATION",
                        "检查制冷与车门",
                        "确认制冷设备运行正常，排查车门密封情况。",
                        "需临时靠边停车检查",
                        "可及时发现设备隐患",
                        false));
            }
            default -> {
                // LOW：按原计划
                list.add(makePlan("FOLLOW_CURRENT_ROUTE",
                        "按原计划配送",
                        "当前温度正常，无需调整路线。",
                        "无",
                        "无",
                        true));
            }
        }

        return list;
    }

    private RecommendationPayload makePlan(
            String planType, String title, String detail,
            String cost, String benefit, boolean recommended) {
        RecommendationPayload rp = new RecommendationPayload();
        rp.setPlanType(planType);
        rp.setTitle(title);
        rp.setDetail(detail);
        rp.setEstimatedCost(cost);
        rp.setEstimatedBenefit(benefit);
        rp.setRecommended(recommended);
        return rp;
    }
}
