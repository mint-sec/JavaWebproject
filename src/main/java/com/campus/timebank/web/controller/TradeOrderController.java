package com.campus.timebank.web.controller;

import com.campus.timebank.common.result.ApiResponse;
import com.campus.timebank.domain.entity.TradeOrder;
import com.campus.timebank.service.TradeOrderService;
import com.campus.timebank.web.dto.OrderCompleteRequest;
import com.campus.timebank.web.dto.TradeOrderCreateRequest;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    public TradeOrderController(TradeOrderService tradeOrderService) {
        this.tradeOrderService = tradeOrderService;
    }

    @PostMapping
    public ApiResponse<TradeOrder> createOrder(@Valid @RequestBody TradeOrderCreateRequest request) {
        return ApiResponse.success(
                tradeOrderService.createOrder(
                        request.getBuyerId(),
                        request.getSkillItemId(),
                        request.getHours(),
                        request.getRemark()
                )
        );
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<TradeOrder> completeOrder(@PathVariable Long id,
                                                 @Valid @RequestBody OrderCompleteRequest request) {
        return ApiResponse.success(tradeOrderService.completeOrder(id, request.getStartTime(), request.getEndTime()));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<TradeOrder> cancelOrder(@PathVariable Long id) {
        return ApiResponse.success(tradeOrderService.cancelOrder(id));
    }

    @GetMapping
    public ApiResponse<List<TradeOrder>> listOrders() {
        return ApiResponse.success(tradeOrderService.list());
    }
}
