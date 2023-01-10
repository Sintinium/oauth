package com.sintinium.oauth.util;

import java.net.URI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sintinium.oauth.OAuth;

public class AgnosticUtils {
    public static JsonElement parseJson(String json) {
        return new JsonParser().parse(json);
    }

    public static boolean isEmpty(JsonArray array) {
        return array == null || array.size() == 0;
    }

    public static void openUri(String uri) {
        try {
            java.awt.Desktop.getDesktop().browse(new URI(uri));
        } catch (Throwable throwable) {
            OAuth.LOGGER.error("Couldn't open link", throwable);
        }
    }
}
