package com.sintinium.oauth.util;

import org.apache.commons.lang3.StringUtils;

public class NullUtils {
    public static <T> T requireNotNull(T obj, String name) {
        if (obj == null) {
            throw new NullPointerException(name + " must not be null");
        }
        if (obj instanceof String) {
            if (StringUtils.isBlank((String) obj)) {
                throw new IllegalArgumentException(name + " must not be blank");
            }
        }
        return obj;
    }
}
