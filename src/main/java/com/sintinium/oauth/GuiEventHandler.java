package com.sintinium.oauth;

import com.sintinium.oauth.gui.LoginTypeScreen;
import com.sintinium.oauth.gui.TextWidget;
import com.sintinium.oauth.login.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "oauth", value = Dist.CLIENT)
public class GuiEventHandler {
    @SubscribeEvent
    public static void multiplayerScreenOpen(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof MultiplayerScreen)) return;
        MultiplayerScreen multiplayerScreen = (MultiplayerScreen) event.getGui();
        try {
//            Method addButtonMethod = ObfuscationReflectionHelper.findMethod(Screen.class, "addButton", Widget.class);
//            Method addButtonMethod = ObfuscationReflectionHelper.findMethod(Screen.class, "func_230480_a_", Widget.class);
            event.addWidget(new Button(10, 6, 66, 20, new StringTextComponent("OAuth Login"), p_onPress_1_ -> Minecraft.getInstance().setScreen(new LoginTypeScreen(multiplayerScreen))));
            final TextWidget textWidget = new TextWidget(10 + 66 + 3, 6, 0, 20, "Status: loading");
            textWidget.setFGColor(0xFFFFFF);
            Thread thread = new Thread(() -> {
                boolean isOnline = LoginUtil.isOnline();
                if (isOnline) {
                    textWidget.setMessage(new StringTextComponent("Status: online"));
                    textWidget.setFGColor(0x55FF55);
                } else {
                    textWidget.setMessage(new StringTextComponent("Status: offline"));
                    textWidget.setFGColor(0xFF5555);
                }
            }, "Oauth status");
            thread.setDaemon(true);
            thread.start();

            event.addWidget(textWidget);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
