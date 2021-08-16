package com.sintinium.oauth.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sintinium.oauth.login.LoginUtil;
import com.sintinium.oauth.login.MicrosoftLogin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;

public class LoginTypeScreen extends OAuthScreen {

    private JoinMultiplayerScreen lastScreen;

    public LoginTypeScreen(JoinMultiplayerScreen last) {
        super(new TextComponent("Select Account Type"));
        lastScreen = last;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 - 20 - 2, 200, 20, new TextComponent("Mojang Login"), p_onPress_1_ -> {
            Minecraft.getInstance().setScreen(new LoginScreen(this, lastScreen));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + 2, 200, 20, new TextComponent("Microsoft Login"), (p_213031_1_) -> {
            final MicrosoftLogin login = new MicrosoftLogin();
            LoginLoadingScreen loadingScreen = new LoginLoadingScreen(lastScreen, this, login::cancelLogin, true);
            login.setUpdateStatusConsumer(loadingScreen::updateText);
            Thread thread = new Thread(() -> {
                login.login(() -> {
                    LoginUtil.updateOnlineStatus();
                    loadingScreen.toRun.add(() -> Minecraft.getInstance().setScreen(lastScreen));
                });
            });
            if (login.getErrorMsg() != null) {
                System.err.println(login.getErrorMsg());
            }
            Minecraft.getInstance().setScreen(loadingScreen);
            thread.start();
        }));

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + 60, 200, 20, CommonComponents.GUI_CANCEL, (p_213029_1_) -> {
            Minecraft.getInstance().setScreen(lastScreen);
        }));
    }

    @Override
    public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        this.renderBackground(p_230430_1_);
        drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, this.height / 2 - 60, 16777215);
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
    }
}
