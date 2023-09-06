package me.jarva.oauth.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.concurrent.atomic.AtomicReference;

public class OAuthScreen extends Screen {
    private static final AtomicReference<Screen> screenToSet = new AtomicReference<>(null);

    protected OAuthScreen(Component title) {
        super((title));
    }

    public static void setScreen(Screen screen) {
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
