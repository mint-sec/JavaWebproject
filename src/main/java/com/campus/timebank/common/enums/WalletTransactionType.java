package com.campus.timebank.common.enums;

import lombok.Getter;

@Getter
public enum WalletTransactionType {
    FREEZE_OUT("FREEZE_OUT"),
    UNFREEZE_IN("UNFREEZE_IN"),
    TRANSFER_IN("TRANSFER_IN");

    private final String code;

    WalletTransactionType(String code) {
        this.code = code;
    }
}
