package com.coldchain.algorithm.service;

import com.coldchain.algorithm.model.EvaluateRequest;
import com.coldchain.algorithm.model.EvaluateResponse.RecommendationPayload;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutePlannerTest {

    private final RoutePlanner planner = new RoutePlanner();

    @Test
    void shouldReturnColdStorageForHighRisk() {
        List<RecommendationPayload> plans = planner.generate("HIGH", telemetry(12.0, false, 26.0));

        assertTrue(plans.size() >= 2);
        assertTrue(plans.get(0).isRecommended());
        assertEquals("REROUTE_COLD_STORAGE", plans.get(0).getPlanType());
    }

    @Test
    void shouldReturnPriorityDeliveryForMediumRiskWhenNearDestination() {
        List<RecommendationPayload> plans = planner.generate("MEDIUM", telemetry(6.0, false, 24.0));

        assertTrue(plans.size() >= 1);
        assertTrue(plans.get(0).isRecommended());
        assertEquals("PRIORITY_DELIVERY", plans.get(0).getPlanType());
    }

    @Test
    void shouldReturnFollowRouteForLowRisk() {
        List<RecommendationPayload> plans = planner.generate("LOW", telemetry(18.0, false, 25.0));

        assertEquals(1, plans.size());
        assertEquals("FOLLOW_CURRENT_ROUTE", plans.get(0).getPlanType());
        assertTrue(plans.get(0).isRecommended());
    }

    private EvaluateRequest.TelemetryPayload telemetry(double remainingKm, boolean doorOpen, double outsideTemp) {
        EvaluateRequest.TelemetryPayload telemetry = new EvaluateRequest.TelemetryPayload();
        telemetry.setRemainingKm(remainingKm);
        telemetry.setDoorOpen(doorOpen);
        telemetry.setOutsideTemp(outsideTemp);
        telemetry.setTemperature(5.0);
        return telemetry;
    }
}
