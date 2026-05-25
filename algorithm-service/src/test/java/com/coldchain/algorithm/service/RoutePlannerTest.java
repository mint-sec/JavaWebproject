package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateResponse.RecommendationPayload;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 路径建议模块单元测试
 */
class RoutePlannerTest {

    private final RoutePlanner planner = new RoutePlanner();

    /** HIGH → 至少 2 条建议，第一条为推荐，类型含冷库 */
    @Test
    void shouldReturnColdStorageForHighRisk() {
        List<RecommendationPayload> plans = planner.generate("HIGH");

        assertTrue(plans.size() >= 2, "HIGH 至少 2 条建议");
        assertTrue(plans.get(0).isRecommended(), "第一条应为推荐方案");
        assertEquals("REROUTE_COLD_STORAGE", plans.get(0).getPlanType());
    }

    /** MEDIUM → 优先配送为推荐 */
    @Test
    void shouldReturnPriorityDeliveryForMediumRisk() {
        List<RecommendationPayload> plans = planner.generate("MEDIUM");

        assertTrue(plans.size() >= 1);
        assertTrue(plans.get(0).isRecommended());
        assertEquals("PRIORITY_DELIVERY", plans.get(0).getPlanType());
    }

    /** LOW → 按原计划 */
    @Test
    void shouldReturnFollowRouteForLowRisk() {
        List<RecommendationPayload> plans = planner.generate("LOW");

        assertEquals(1, plans.size());
        assertEquals("FOLLOW_CURRENT_ROUTE", plans.get(0).getPlanType());
        assertTrue(plans.get(0).isRecommended());
    }
}
