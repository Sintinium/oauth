package com.sintinium.oauth.gui;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Splitter;
import com.sintinium.oauth.OAuth;
import com.sintinium.oauth.login.MicrosoftLogin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class ErrorScreen extends GuiScreenCustom {
    private String message = null;
    private Throwable e = null;
    private boolean isInfo = false;
    private String title;
    private final GuiScreen lastScreen;

    public ErrorScreen(GuiScreen last, boolean isMs, String message) {
    	lastScreen = last;
    	title = "Error logging into " + (isMs ? "Microsoft." : "Mojang.");
        this.message = message;
        System.err.println(message);
    }

    public ErrorScreen(GuiScreen last, boolean isMs, Throwable e) {
    	lastScreen = last;
    	title = "Error logging into " + (isMs ? "Microsoft." : "Mojang.");
        this.e = e;
        e.printStackTrace();
    }

    public void setInfo() {
        this.isInfo = true;
    }

    @Override
    public void initGui() {
        this.addButton(new ActionButton(0, this.width / 2 - 100, this.height / 2 + 60, 200, 20, "Cancel", () -> setScreen(lastScreen)));
    }

    public static ErrorScreen microsoftExceptionScreen(GuiScreen last, MicrosoftLogin.BaseMicrosoftLoginException e) {
        ErrorScreen screen = null;
        if (e instanceof MicrosoftLogin.NoXboxAccountException) {
            screen = new ErrorScreen(last, true, "This account has no Microsoft/Xbox account. Please login through minecraft.net to create one.");
        } else if (e instanceof MicrosoftLogin.BannedCountryException) {
            screen = new ErrorScreen(last, true, "This account is from a country where Xbox Live is not available/banned.");
        } else if (e instanceof MicrosoftLogin.UnderageAccountException) {
            screen = new ErrorScreen(last, true, "This account is under 18 and doesn't work with 3rd party logins.\nEither change your account's age or have an adult setup a family group.");
        } else if (e instanceof MicrosoftLogin.NoAccountFoundException) {
            screen = new ErrorScreen(last, true, "This account doesn't own Minecraft.\nIf you're a gamepass user make sure to login through the new launcher first.");
        } else {
            throw new IllegalStateException("Unknown MicrosoftLoginException: " + e.getClass().getName());
        }
        screen.setInfo();
        return screen;
    }

    private String getMessage() {
        String result = "";
        if (message != null) {
            result = message;
        } else if (e != null) {
            result = ExceptionUtils.getStackTrace(e);
        } else {
            return "Error getting error message.";
        }
        return result;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);
        if (isInfo) {
            drawCenteredString(Minecraft.getMinecraft().fontRenderer, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);

            Iterable<String> messages = Splitter.on("\n").split(getMessage());
            int index = 0;
            for (String m : messages) {
            	Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(m, (this.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(m)) / 2, (this.height - 48) / 2 + (index * 12), 0xFF4444);
                index++;
            }
        } else if (getMessage().toLowerCase().contains("no such host is known") || getMessage().toLowerCase().contains("connection reset")) {
            drawCenteredString(Minecraft.getMinecraft().fontRenderer, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            drawCenteredString(Minecraft.getMinecraft().fontRenderer, "The servers could be down or it could be an internet problem.", this.width / 2, this.height / 2 - 28, 0xFFFFFF);
            drawCenteredString(Minecraft.getMinecraft().fontRenderer, "If you believe this is a bug please create an issue at", this.width / 2, this.height / 2 - 12, 0xFFFFFF);
            drawCenteredString(Minecraft.getMinecraft().fontRenderer, "https://github.com/Sintinium/oauth with your latest log file.", this.width / 2, this.height / 2, 0xFFFFFF);
        } else {
            drawCenteredString(Minecraft.getMinecraft().fontRenderer, "An error occurred. This could be a bug.", this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            drawCenteredString(Minecraft.getMinecraft().fontRenderer, "Please create an issue at \u00a7nhttps://github.com/Sintinium/oauth\u00a7r with your log file.", this.width / 2, this.height / 2 - 28, 0xFFFFFF);
            float scale = .5f;
            GL11.glScalef(scale, scale, scale);
            String msg = getMessage();
            if (OAuth.INSTANCE != null && OAuth.INSTANCE.modContainer != null) {
                msg = "OAuth Forge v" + OAuth.INSTANCE.modContainer.getVersion() + ": " + msg;
            }
            Iterable<String> messages = Splitter.fixedLength(Math.round(80 * (1f / scale))).limit(12).split(msg);
            int index = 0;
            for (String m : messages) {
            	Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(m, (int) (this.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(m) * scale) / 2, (int)((this.height - 32) / 2 * (1 / scale)) + (index * 12), 0xFF4444);
                index++;
            }
            GL11.glScalef(1f / scale, 1f / scale, 1f / scale);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
