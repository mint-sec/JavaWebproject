package com.campus.timebank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.timebank.domain.entity.Review;

public interface ReviewService extends IService<Review> {

    Review createReview(Review review);
}
