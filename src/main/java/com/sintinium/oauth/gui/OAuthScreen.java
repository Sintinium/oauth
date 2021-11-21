package com.sintinium.oauth.gui;

import com.sintinium.oauth.OAuth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public class OAuthScreen extends Screen {
    protected OAuthScreen(ITextComponent pTitle) {
        super(pTitle);
    }

    @Override
    public void tick() {
        super.tick();
        Screen screenToSet = OAuth.getInstance().screenToSet.getAndSet(null);
        if (screenToSet != null) {
            Minecraft.getInstance().setScreen(screenToSet);
        }
    }
}
