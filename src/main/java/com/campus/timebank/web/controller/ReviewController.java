package com.campus.timebank.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.timebank.common.result.ApiResponse;
import com.campus.timebank.domain.entity.Review;
import com.campus.timebank.service.ReviewService;
import com.campus.timebank.web.dto.ReviewCreateRequest;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ApiResponse<Review> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        Review review = new Review();
        BeanUtils.copyProperties(request, review);
        return ApiResponse.success(reviewService.createReview(review));
    }

    @GetMapping
    public ApiResponse<List<Review>> listReviews(@RequestParam(required = false) Long skillItemId,
                                                 @RequestParam(required = false) Long revieweeId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(skillItemId != null, Review::getSkillItemId, skillItemId);
        wrapper.eq(revieweeId != null, Review::getRevieweeId, revieweeId);
        wrapper.orderByDesc(Review::getId);
        return ApiResponse.success(reviewService.list(wrapper));
    }
}
