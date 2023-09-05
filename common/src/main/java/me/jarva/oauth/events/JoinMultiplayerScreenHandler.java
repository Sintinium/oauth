package me.jarva.oauth.events;

import me.jarva.oauth.gui.MultiplayerDisabledScreen;
import me.jarva.oauth.gui.components.OAuthButton;
import me.jarva.oauth.gui.components.TextWidget;
import me.jarva.oauth.gui.profile.ProfileSelectionScreen;
import me.jarva.oauth.util.ComponentUtils;
import me.jarva.oauth.util.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

public class JoinMultiplayerScreenHandler {
    public static boolean warned = false;

    public static void handle(Minecraft client, JoinMultiplayerScreen screen) {
        if (LoginUtil.isMultiplayerDisabled() && !warned) {
            client.setScreen(new MultiplayerDisabledScreen());
            warned = true;
            return;
        }

        try {
            screen.addRenderableWidget(new OAuthButton(10, 6, 66, 20, ComponentUtils.literal("OAuth Login"), button -> client.setScreen(new ProfileSelectionScreen())));
            final TextWidget textWidget = new TextWidget(10 + 66 + 3, 6, 0, 20, "Status: loading");
            textWidget.setFGColor(0xFFFFF);
            Thread thread = new Thread(() -> {
                boolean isOnline = LoginUtil.isOnline();
                if (isOnline) {
                    textWidget.setMessage(ComponentUtils.literal("Status: online"));
                    textWidget.setFGColor(0x55FF55);
                } else {
                    textWidget.setMessage(ComponentUtils.literal("Status: offline"));
                    textWidget.setFGColor(0xFF5555);
                }
            }, "OAuth Status");
            thread.setDaemon(true);
            thread.start();

            screen.addRenderableWidget(textWidget);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
