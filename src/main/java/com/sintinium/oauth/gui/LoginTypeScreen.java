package com.sintinium.oauth.gui;

import com.sintinium.oauth.login.LoginUtil;
import com.sintinium.oauth.login.MicrosoftLogin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;

public class LoginTypeScreen extends GuiScreen {

    private GuiMultiplayer lastScreen;

    private int mojangButtonId = 0;
    private int microsoftLoginId = 1;
    private int cancelId = 2;

    public LoginTypeScreen(GuiMultiplayer last) {
        lastScreen = last;
    }


    @Override
    public void initGui() {
        this.addButton(new ActionButton(mojangButtonId, this.width / 2 - 100, this.height / 2 - 20 - 2, 200, 20, "Mojang Login", () -> {
            Minecraft.getMinecraft().displayGuiScreen(new LoginScreen(this, lastScreen));
        }));
        this.addButton(new ActionButton(microsoftLoginId, this.width / 2 - 100, this.height / 2 + 2, 200, 20, "Microsoft Login", () -> {
            final MicrosoftLogin login = new MicrosoftLogin();
            LoginLoadingScreen loadingScreen = new LoginLoadingScreen(lastScreen, this, login::cancelLogin, true);
            login.setUpdateStatusConsumer(loadingScreen::updateText);
            Thread thread = new Thread(() -> {
                try {
                    login.login(() -> {
                        LoginUtil.updateOnlineStatus();
                        Minecraft.getMinecraft().displayGuiScreen(lastScreen);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Minecraft.getMinecraft().displayGuiScreen(new ErrorScreen(true, e));
                }
            });
            Minecraft.getMinecraft().displayGuiScreen(loadingScreen);
            thread.setDaemon(true);
            thread.start();
        }));

        this.addButton(new ActionButton(cancelId, this.width / 2 - 100, this.height / 2 + 60, 200, 20, "Cancel", () -> {
            Minecraft.getMinecraft().displayGuiScreen(lastScreen);
        }));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof ActionButton) {
            ((ActionButton) button).onClicked();
        } else {
            throw new RuntimeException("Missing button action!");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground(0);
        drawCenteredString(Minecraft.getMinecraft().fontRenderer, "Select Account Type", this.width / 2, this.height / 2 - 60, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }


}
