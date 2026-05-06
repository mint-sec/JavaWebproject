package com.campus.timebank.common.enums;

import lombok.Getter;

@Getter
public enum SkillStatus {
    PENDING_REVIEW(0, "pending_review"),
    ON_SHELF(1, "on_shelf"),
    OFF_SHELF(2, "off_shelf");

    private final Integer code;
    private final String desc;

    SkillStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
