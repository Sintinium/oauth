package me.jarva.oauth.gui.components;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class OAuthButton extends Button {
    public OAuthButton(int x, int y, int width, int height, Component text, Button.OnPress onPress) {
        super(x, y, width, 20, text, onPress, Button.DEFAULT_NARRATION);
    }
}
