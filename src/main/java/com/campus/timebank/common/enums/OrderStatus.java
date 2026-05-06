package com.campus.timebank.common.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    CREATED(10, "created"),
    IN_SERVICE(20, "in_service"),
    COMPLETED(30, "completed"),
    CANCELLED(40, "cancelled");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
