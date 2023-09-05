package me.jarva.oauth.util;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class ComponentUtils {
    public static MutableComponent literal(String text) {
        #if POST_MC_1_18_2
        return Component.literal(text);
        #else
        return new TextComponent(text);
        #endif
    }

    public static MutableComponent empty() {
        #if POST_MC_1_18_2
        return Component.empty();
        #else
        return TextComponent.EMPTY.copy();
        #endif
    }
}
