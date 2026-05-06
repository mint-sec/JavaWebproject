package com.campus.timebank.web.dto;

import java.math.BigDecimal;
import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Data
public class TradeOrderCreateRequest {

    @NotNull(message = "buyerId is required")
    private Long buyerId;

    @NotNull(message = "skillItemId is required")
    private Long skillItemId;

    @NotNull(message = "hours is required")
    @DecimalMin(value = "0.50", message = "hours must be at least 0.5")
    private BigDecimal hours;

    private String remark;
}
