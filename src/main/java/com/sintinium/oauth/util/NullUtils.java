package com.sintinium.oauth.util;

public class NullUtils {
    public static void requireNotNull(Object obj, String name) {
        if (obj == null) {
            throw new NullPointerException(name + " must not be null");
        }
    }
}
