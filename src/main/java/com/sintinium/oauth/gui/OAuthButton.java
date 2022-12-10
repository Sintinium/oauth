package com.sintinium.oauth.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class OAuthButton {

    public static Button create(int x, int y, int width, int height, Component component, Button.OnPress onPress) {
        var builder = Button.builder(component, onPress);
        builder.bounds(x, y, width, height);
        return builder.build();
    }

}
