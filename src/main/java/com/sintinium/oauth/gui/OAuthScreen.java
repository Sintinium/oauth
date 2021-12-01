package com.sintinium.oauth.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;

import java.util.concurrent.atomic.AtomicReference;

public class OAuthScreen extends Screen {
    private static final AtomicReference<Screen> screenToSet = new AtomicReference<>(null);

    protected OAuthScreen(Component pTitle) {
        super(pTitle);
    }

    /**
     * Safely sets screen even if called in a different thread.
     */
    public static void setScreen(Screen screen) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT) {
            Minecraft.getInstance().setScreen(screen);
            return;
        }
        screenToSet.set(screen);
    }

    @Override
    public void tick() {
        super.tick();
        Screen screenToSet = OAuthScreen.screenToSet.getAndSet(null);
        if (screenToSet != null) {
            Minecraft.getInstance().setScreen(screenToSet);
        }
    }
}
