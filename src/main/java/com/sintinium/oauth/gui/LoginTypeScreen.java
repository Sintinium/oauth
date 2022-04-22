package com.sintinium.oauth.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class LoginTypeScreen extends GuiScreenCustom {
    private final Runnable onMojang;
    private final Runnable onMicrosoft;
    private GuiScreen lastScreen;

    private static final int mojangButtonId = 0;
    private static final int microsoftLoginId = 1;
    private static final int cancelId = 2;

    public LoginTypeScreen(GuiScreen lastScreen, Runnable onMojang, Runnable onMicrosoft) {
    	this.lastScreen = lastScreen;
        this.onMojang = onMojang;
        this.onMicrosoft = onMicrosoft;
    }

//    @Override
//    public void initGui() {
//        this.addButton(new ActionButton(mojangButtonId, this.width / 2 - 100, this.height / 2 - 20 - 2, 200, 20, "Mojang Login", () -> {
//            Minecraft.getMinecraft().displayGuiScreen(new LoginScreen(this, lastScreen));
//        }));
//        this.addButton(new ActionButton(microsoftLoginId, this.width / 2 - 100, this.height / 2 + 2, 200, 20, "Microsoft Login", () -> {
//            final MicrosoftLogin login = new MicrosoftLogin();
//            LoginLoadingScreen loadingScreen = new LoginLoadingScreen(lastScreen, this, login::cancelLogin, true);
//            login.setUpdateStatusConsumer(loadingScreen::updateText);
//            Thread thread = new Thread(() -> login.login(() -> {
//                LoginUtil.updateOnlineStatus();
//                Minecraft.getMinecraft().displayGuiScreen(lastScreen);
//            }));
//            if (login.getErrorMsg() != null) {
//                System.err.println(login.getErrorMsg());
//            }
//            Minecraft.getMinecraft().displayGuiScreen(loadingScreen);
//            thread.start();
//        }));
//
//        this.addButton(new ActionButton(cancelId, this.width / 2 - 100, this.height / 2 + 60, 200, 20, "Cancel", () -> {
//            Minecraft.getMinecraft().displayGuiScreen(lastScreen);
//        }));
//    }

    @Override
	public void initGui() {
        this.addButton(new ActionButton(mojangButtonId, this.width / 2 - 100, this.height / 2 - 20 - 2, 200, 20, "Mojang Login", () -> {
            this.onMojang.run();
        }));
        this.addButton(new ActionButton(microsoftLoginId, this.width / 2 - 100, this.height / 2 + 2, 200, 20, "Microsoft Login", () -> {
//          final MicrosoftLogin login = new MicrosoftLogin();
//          LoginLoadingScreen loadingScreen = new LoginLoadingScreen(new ProfileSelectionScreen(), this, login::cancelLogin, true);
//          login.setUpdateStatusConsumer(loadingScreen::updateText);
//          Thread thread = new Thread(() -> {
//              login.login(() -> {
//                  LoginUtil.updateOnlineStatus();
//                  OAuth.getInstance().setScreen(new ProfileSelectionScreen());
//              });
//          }, "Oauth microsoft");
//          if (login.getErrorMsg() != null) {
//              System.err.println(login.getErrorMsg());
//          }
//          OAuth.getInstance().setScreen(loadingScreen);
//          thread.setDaemon(true);
//          thread.start();
            this.onMicrosoft.run();
        }));
//      this.addButton(new Button(this.width / 2 - 100 + 200 - 50, this.height / 2 + 2, 50, 20, new StringTextComponent("Logout"), p_onPress_1_ -> {
//          MicrosoftLogin.logout();
//      }));
        this.addButton(new ActionButton(cancelId, this.width / 2 - 100, this.height / 2 + 60, 200, 20, "Cancel", () -> setScreen(lastScreen)));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground(0);
        drawCenteredString(Minecraft.getMinecraft().fontRenderer, "Select Account Type", this.width / 2, this.height / 2 - 60, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
