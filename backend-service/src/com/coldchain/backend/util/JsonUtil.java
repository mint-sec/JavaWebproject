package com.coldchain.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static String formatDouble(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public static <T> String toJsonArray(List<T> items, Function<T, String> mapper) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(mapper.apply(items.get(i)));
        }
        builder.append("]");
        return builder.toString();
    }

    public static String ok(String dataJson) {
        return "{\"success\":true,\"message\":\"ok\",\"data\":" + dataJson + "}";
    }

    public static String error(String message) {
        return "{\"success\":false,\"message\":\"" + escape(message) + "\",\"data\":null}";
    }
}
