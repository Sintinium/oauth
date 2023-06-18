package com.sintinium.oauth.gui.components;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class OAuthButton extends Button {

    public OAuthButton(int x, int y, int width, int height, String text, Button.OnPress onPress) {
        super(x, y, width, 20, Component.literal(text), onPress, Button.DEFAULT_NARRATION);
    }

    public OAuthButton(int x, int y, int width, int height, Component component, Button.OnPress onPress) {
        super(x, y, width, 20, component, onPress, Button.DEFAULT_NARRATION);
    }
}
