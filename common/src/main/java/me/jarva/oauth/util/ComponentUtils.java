package me.jarva.oauth.util;

#if POST_MC_1_18_2
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
#else
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
#endif

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
