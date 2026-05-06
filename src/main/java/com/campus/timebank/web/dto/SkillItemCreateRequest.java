package com.campus.timebank.web.dto;

import java.math.BigDecimal;
import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SkillItemCreateRequest {

    @NotNull(message = "ownerId is required")
    private Long ownerId;

    @NotBlank(message = "title cannot be blank")
    private String title;

    @NotBlank(message = "description cannot be blank")
    private String description;

    @NotNull(message = "pricePerHour is required")
    @DecimalMin(value = "0.01", message = "pricePerHour must be greater than 0")
    private BigDecimal pricePerHour;

    @NotBlank(message = "category cannot be blank")
    private String category;

    private Integer capacity;

    private String tags;
}
