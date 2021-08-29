package com.sintinium.oauth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.sintinium.oauth.login.LoginUtil;
import com.sintinium.oauth.login.MicrosoftLogin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class LoginTypeScreen extends Screen {

    private MultiplayerScreen lastScreen;

    public LoginTypeScreen(MultiplayerScreen last) {
        super(new StringTextComponent("Select Account Type"));
        lastScreen = last;
    }

    @Override
    protected void init() {
        this.addButton(new Button(this.width / 2 - 100, this.height / 2 - 20 - 2, 200, 20, new StringTextComponent("Mojang Login"), p_onPress_1_ -> {
            Minecraft.getInstance().setScreen(new LoginScreen(this, lastScreen));
        }));
        this.addButton(new Button(this.width / 2 - 100, this.height / 2 + 2, 200 - 52, 20, new StringTextComponent("Microsoft Login"), (p_213031_1_) -> {
            final MicrosoftLogin login = new MicrosoftLogin();
            LoginLoadingScreen loadingScreen = new LoginLoadingScreen(lastScreen, this, login::cancelLogin, true);
            login.setUpdateStatusConsumer(loadingScreen::updateText);
            Thread thread = new Thread(() -> {
                login.login(() -> {
                    LoginUtil.updateOnlineStatus();
                    Minecraft.getInstance().setScreen(lastScreen);
                });
            });
            if (login.getErrorMsg() != null) {
                System.err.println(login.getErrorMsg());
            }
            Minecraft.getInstance().setScreen(loadingScreen);
            thread.start();
        }));

        final List<ITextProperties> logoutTooltip = new ArrayList<>();
        logoutTooltip.add(new StringTextComponent("Logout of your Microsoft account to switch accounts."));
        this.addButton(new Button(this.width / 2 - 100 + 200 - 50, this.height / 2 + 2, 50, 20, new StringTextComponent("Logout"), p_onPress_1_ -> {
            MicrosoftLogin.logout();
        }, (button, matrix, x, y) -> {
            renderWrappedToolTip(matrix,
                    logoutTooltip,
                    x, y, this.font
            );
        }));

        this.addButton(new Button(this.width / 2 - 100, this.height / 2 + 60, 200, 20, DialogTexts.GUI_CANCEL, (p_213029_1_) -> {
            Minecraft.getInstance().setScreen(lastScreen);
        }));
    }

    @Override
    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        this.renderBackground(p_230430_1_);
        drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, this.height / 2 - 60, 16777215);
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
    }
}
