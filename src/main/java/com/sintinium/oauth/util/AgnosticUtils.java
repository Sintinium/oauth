package com.sintinium.oauth.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.Util;

public class AgnosticUtils {
    public static JsonElement parseJson(String json) {
        return JsonParser.parseString(json);
    }

    public static boolean isEmpty(JsonArray array) {
        return array == null || array.size() == 0;
    }

    public static void openUri(String uri) {
        Util.getPlatform().openUri(uri);
    }
}
