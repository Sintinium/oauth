package com.sintinium.oauth;

import com.sintinium.oauth.gui.TextWidget;
import com.sintinium.oauth.gui.profile.ProfileSelectionScreen;
import com.sintinium.oauth.login.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "oauth", value = Dist.CLIENT)
public class GuiEventHandler {
    @SubscribeEvent
    public static void multiplayerScreenOpen(ScreenEvent.InitScreenEvent.Post event) {
        if (!(event.getScreen() instanceof JoinMultiplayerScreen)) return;
        JoinMultiplayerScreen multiplayerScreen = (JoinMultiplayerScreen) event.getScreen();
        try {
//            Method addButtonMethod = ObfuscationReflectionHelper.findMethod(Screen.class, "addButton", Widget.class);
//            Method addButtonMethod = ObfuscationReflectionHelper.findMethod(Screen.class, "func_230480_a_", Widget.class);
            event.addListener(new Button(10, 6, 66, 20, new TextComponent("OAuth Login"), p_onPress_1_ -> Minecraft.getInstance().setScreen(new ProfileSelectionScreen())));
            final TextWidget textWidget = new TextWidget(10 + 66 + 3, 6, 0, 20, "Status: loading");
            textWidget.setFGColor(0xFFFFFF);
            Thread thread = new Thread(() -> {
                boolean isOnline = LoginUtil.isOnline();
                if (isOnline) {
                    textWidget.setMessage(new TextComponent("Status: online"));
                    textWidget.setFGColor(0x55FF55);
                } else {
                    textWidget.setMessage(new TextComponent("Status: offline"));
                    textWidget.setFGColor(0xFF5555);
                }
            }, "Oauth status");
            thread.setDaemon(true);
            thread.start();

            event.addListener(textWidget);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
