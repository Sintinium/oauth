package com.sintinium.oauth;

import com.sintinium.oauth.gui.LoginTypeScreen;
import com.sintinium.oauth.gui.TextWidget;
import com.sintinium.oauth.login.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(modid = "oauth", clientSideOnly = true)
public class OAuth {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private static TextWidget statusText = new TextWidget(10 + 66 + 3, 12, "Status: offline");

    public OAuth() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void multiplayerScreenOpen(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiMultiplayer)) return;
        GuiMultiplayer multiplayerScreen = (GuiMultiplayer) event.getGui();
        try {
//            Method addButtonMethod = ObfuscationReflectionHelper.findMethod(Screen.class, "addButton", Widget.class);
//            Method addButtonMethod = ObfuscationReflectionHelper.findMethod(Screen.class, "func_230480_a_", Widget.class);
            List<GuiButton> buttonList = new ArrayList<>();
            GuiButton loginButton = new GuiButton(29183, 10, 6, 66, 20, "Oauth Login");
            // p_onPress_1_ ->
            buttonList.add(loginButton);
            Thread thread = new Thread(() -> {
                boolean isOnline = LoginUtil.isOnline();
                if (isOnline) {
                    statusText.setText("Status: online");
                    statusText.setColor(0x55FF55);
                } else {
                    statusText.setText("Status: offline");
                    statusText.setColor(0xFF5555);
                }
            });
            thread.start();

            event.getButtonList().addAll(buttonList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void multiplayerScreenDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof GuiMultiplayer)) return;
        statusText.draw(event.getGui());
    }

    @SubscribeEvent
    public void action(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (!(event.getGui() instanceof GuiMultiplayer)) return;
        if (event.getButton().id != 29183) return;
        GuiMultiplayer multiplayerScreen = (GuiMultiplayer) event.getGui();
        Minecraft.getMinecraft().displayGuiScreen(new LoginTypeScreen(multiplayerScreen));
    }
}
