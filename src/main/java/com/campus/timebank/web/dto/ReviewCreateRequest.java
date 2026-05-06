package com.campus.timebank.web.dto;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReviewCreateRequest {

    @NotNull(message = "orderId is required")
    private Long orderId;

    @NotNull(message = "skillItemId is required")
    private Long skillItemId;

    @NotNull(message = "reviewerId is required")
    private Long reviewerId;

    @NotNull(message = "revieweeId is required")
    private Long revieweeId;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;

    @NotBlank(message = "content cannot be blank")
    private String content;
}
