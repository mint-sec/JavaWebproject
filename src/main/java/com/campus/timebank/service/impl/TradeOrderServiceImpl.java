package com.campus.timebank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.timebank.common.enums.OrderStatus;
import com.campus.timebank.common.enums.SkillStatus;
import com.campus.timebank.common.enums.WalletTransactionType;
import com.campus.timebank.common.exception.BizException;
import com.campus.timebank.domain.entity.SkillItem;
import com.campus.timebank.domain.entity.TradeOrder;
import com.campus.timebank.domain.entity.User;
import com.campus.timebank.domain.entity.WalletTransaction;
import com.campus.timebank.mapper.TradeOrderMapper;
import com.campus.timebank.mapper.WalletTransactionMapper;
import com.campus.timebank.service.SkillItemService;
import com.campus.timebank.service.TradeOrderService;
import com.campus.timebank.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements TradeOrderService {

    private final UserService userService;
    private final SkillItemService skillItemService;
    private final WalletTransactionMapper walletTransactionMapper;

    public TradeOrderServiceImpl(UserService userService,
                                 SkillItemService skillItemService,
                                 WalletTransactionMapper walletTransactionMapper) {
        this.userService = userService;
        this.skillItemService = skillItemService;
        this.walletTransactionMapper = walletTransactionMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder createOrder(Long buyerId, Long skillItemId, BigDecimal hours, String remark) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("hours must be greater than 0");
        }

        User buyer = userService.getById(buyerId);
        SkillItem skillItem = skillItemService.getById(skillItemId);
        if (buyer == null) {
            throw new BizException("buyer not found");
        }
        if (skillItem == null) {
            throw new BizException("skill item not found");
        }
        if (!SkillStatus.ON_SHELF.getCode().equals(skillItem.getStatus())) {
            throw new BizException("skill item is not available");
        }
        if (buyerId.equals(skillItem.getOwnerId())) {
            throw new BizException("cannot buy your own service");
        }

        BigDecimal totalCoin = skillItem.getPricePerHour().multiply(hours);
        if (buyer.getTimeCoin().compareTo(totalCoin) < 0) {
            throw new BizException("insufficient time coin balance");
        }

        buyer.setTimeCoin(buyer.getTimeCoin().subtract(totalCoin));
        buyer.setFrozenCoin(buyer.getFrozenCoin().add(totalCoin));
        userService.updateById(buyer);

        TradeOrder order = new TradeOrder();
        order.setBuyerId(buyerId);
        order.setSellerId(skillItem.getOwnerId());
        order.setSkillItemId(skillItemId);
        order.setHours(hours);
        order.setTotalCoin(totalCoin);
        order.setStatus(OrderStatus.CREATED.getCode());
        order.setRemark(remark);
        save(order);

        walletTransactionMapper.insert(buildWalletTransaction(
                buyerId,
                order.getId(),
                WalletTransactionType.FREEZE_OUT.getCode(),
                totalCoin.negate(),
                buyer.getTimeCoin(),
                "freeze coins for order"
        ));

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder completeOrder(Long orderId, LocalDateTime startTime, LocalDateTime endTime) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            throw new BizException("order not found");
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new BizException("endTime must be after startTime");
        }
        if (!OrderStatus.CREATED.getCode().equals(order.getStatus())
                && !OrderStatus.IN_SERVICE.getCode().equals(order.getStatus())) {
            throw new BizException("order status does not allow completion");
        }

        User buyer = userService.getById(order.getBuyerId());
        User seller = userService.getById(order.getSellerId());
        if (buyer == null || seller == null) {
            throw new BizException("order user missing");
        }
        if (buyer.getFrozenCoin().compareTo(order.getTotalCoin()) < 0) {
            throw new BizException("frozen coin is inconsistent");
        }

        buyer.setFrozenCoin(buyer.getFrozenCoin().subtract(order.getTotalCoin()));
        seller.setTimeCoin(seller.getTimeCoin().add(order.getTotalCoin()));
        userService.updateById(buyer);
        userService.updateById(seller);

        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setStartTime(startTime);
        order.setEndTime(endTime);
        updateById(order);

        walletTransactionMapper.insert(buildWalletTransaction(
                seller.getId(),
                orderId,
                WalletTransactionType.TRANSFER_IN.getCode(),
                order.getTotalCoin(),
                seller.getTimeCoin(),
                "order completed, seller received coins"
        ));

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder cancelOrder(Long orderId) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            throw new BizException("order not found");
        }
        if (!OrderStatus.CREATED.getCode().equals(order.getStatus())) {
            throw new BizException("only created order can be cancelled");
        }

        User buyer = userService.getById(order.getBuyerId());
        if (buyer == null) {
            throw new BizException("buyer not found");
        }
        if (buyer.getFrozenCoin().compareTo(order.getTotalCoin()) < 0) {
            throw new BizException("frozen coin is inconsistent");
        }

        buyer.setFrozenCoin(buyer.getFrozenCoin().subtract(order.getTotalCoin()));
        buyer.setTimeCoin(buyer.getTimeCoin().add(order.getTotalCoin()));
        userService.updateById(buyer);

        order.setStatus(OrderStatus.CANCELLED.getCode());
        updateById(order);

        walletTransactionMapper.insert(buildWalletTransaction(
                buyer.getId(),
                orderId,
                WalletTransactionType.UNFREEZE_IN.getCode(),
                order.getTotalCoin(),
                buyer.getTimeCoin(),
                "cancel order and return frozen coins"
        ));
        return order;
    }

    private WalletTransaction buildWalletTransaction(Long userId,
                                                     Long orderId,
                                                     String type,
                                                     BigDecimal changeAmount,
                                                     BigDecimal balanceAfter,
                                                     String remark) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUserId(userId);
        transaction.setOrderId(orderId);
        transaction.setTransactionType(type);
        transaction.setChangeAmount(changeAmount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRemark(remark);
        return transaction;
    }
}
