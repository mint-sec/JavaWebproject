package com.campus.timebank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.timebank.domain.entity.TradeOrder;
import java.time.LocalDateTime;

public interface TradeOrderService extends IService<TradeOrder> {

    TradeOrder createOrder(Long buyerId, Long skillItemId, java.math.BigDecimal hours, String remark);

    TradeOrder completeOrder(Long orderId, LocalDateTime startTime, LocalDateTime endTime);

    TradeOrder cancelOrder(Long orderId);
}
