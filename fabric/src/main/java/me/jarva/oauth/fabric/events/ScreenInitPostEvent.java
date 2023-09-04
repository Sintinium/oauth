package me.jarva.oauth.fabric.events;

import me.jarva.oauth.events.JoinMultiplayerScreenHandler;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

public class ScreenInitPostEvent {
    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof JoinMultiplayerScreen multiplayerScreen) {
                JoinMultiplayerScreenHandler.handle(client, multiplayerScreen);
            }
        });
    }
}
