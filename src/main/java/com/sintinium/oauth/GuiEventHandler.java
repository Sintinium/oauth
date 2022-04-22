package com.sintinium.oauth;

import com.sintinium.oauth.gui.TextWidget;
import com.sintinium.oauth.gui.profile.ProfileSelectionScreen;
import com.sintinium.oauth.login.LoginUtil;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.client.event.GuiScreenEvent;

public class GuiEventHandler {
	private static final int OAUTH_BUTTON_ID = 29183;
    private static TextWidget statusText = new TextWidget(10 + 66 + 3, 12, "Status: loading");

    @SuppressWarnings("unchecked")
	@SubscribeEvent
    public void multiplayerScreenOpen(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.gui instanceof GuiMultiplayer)) return;
        GuiMultiplayer multiplayerScreen = (GuiMultiplayer) event.gui;
        try {
//            Method addButtonMethod = ObfuscationReflectionHelper.findMethod(Screen.class, "addButton", Widget.class);
//            Method addButtonMethod = ObfuscationReflectionHelper.findMethod(Screen.class, "func_230480_a_", Widget.class);
            GuiButton loginButton = new GuiButton(OAUTH_BUTTON_ID, 10, 6, 66, 20, "Oauth Login");
            event.buttonList.add(loginButton);
            Thread thread = new Thread(() -> {
                boolean isOnline = LoginUtil.isOnline();
                if (isOnline) {
                    statusText.setText("Status: online");
                    statusText.setColor(0x55FF55);
                } else {
                    statusText.setText("Status: offline");
                    statusText.setColor(0xFF5555);
                }
            }, "Oauth status");
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void multiplayerScreenDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.gui instanceof GuiMultiplayer)) return;
        statusText.draw(event.gui);
    }

    @SubscribeEvent
    public void action(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (!(event.gui instanceof GuiMultiplayer)) return;
        if (event.button.id != OAUTH_BUTTON_ID) return;
        GuiMultiplayer multiplayerScreen = (GuiMultiplayer) event.gui;
        Minecraft.getMinecraft().displayGuiScreen(new ProfileSelectionScreen(multiplayerScreen));
    }
}
