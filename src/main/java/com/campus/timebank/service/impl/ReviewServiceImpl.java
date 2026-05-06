package com.campus.timebank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.timebank.common.enums.OrderStatus;
import com.campus.timebank.common.exception.BizException;
import com.campus.timebank.domain.entity.Review;
import com.campus.timebank.domain.entity.TradeOrder;
import com.campus.timebank.mapper.ReviewMapper;
import com.campus.timebank.service.ReviewService;
import com.campus.timebank.service.TradeOrderService;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    private final TradeOrderService tradeOrderService;

    public ReviewServiceImpl(TradeOrderService tradeOrderService) {
        this.tradeOrderService = tradeOrderService;
    }

    @Override
    public Review createReview(Review review) {
        TradeOrder order = tradeOrderService.getById(review.getOrderId());
        if (order == null) {
            throw new BizException("order not found");
        }
        if (!OrderStatus.COMPLETED.getCode().equals(order.getStatus())) {
            throw new BizException("review is only allowed after completion");
        }
        if (!order.getSkillItemId().equals(review.getSkillItemId())) {
            throw new BizException("skill item does not match order");
        }
        boolean validReviewer = review.getReviewerId().equals(order.getBuyerId())
                || review.getReviewerId().equals(order.getSellerId());
        if (!validReviewer) {
            throw new BizException("reviewer is not part of this order");
        }
        boolean validReviewee = review.getReviewerId().equals(order.getBuyerId())
                ? review.getRevieweeId().equals(order.getSellerId())
                : review.getRevieweeId().equals(order.getBuyerId());
        if (!validReviewee) {
            throw new BizException("reviewee does not match order counterpart");
        }
        boolean exists = lambdaQuery()
                .eq(Review::getOrderId, review.getOrderId())
                .eq(Review::getReviewerId, review.getReviewerId())
                .exists();
        if (exists) {
            throw new BizException("duplicate review is not allowed");
        }
        save(review);
        return review;
    }
}
