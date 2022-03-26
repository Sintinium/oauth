package com.sintinium.oauth.gui;

import com.google.common.base.Splitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;

public class ErrorScreen extends GuiScreen {

    private String title;
    private String message = null;
    private Throwable e = null;
    private boolean isInfo = false;

    public ErrorScreen(boolean isMs, String message) {
        title = "Error logging into " + (isMs ? "Microsoft." : "Mojang.");
        this.message = message;
        System.err.println(message);
    }

    public ErrorScreen(boolean isMs, Throwable e) {
        title = "Error logging into " + (isMs ? "Microsoft." : "Mojang.");
        this.e = e;
        e.printStackTrace();
    }

    public void setInfo() {
        this.isInfo = true;
    }

    @Override
    public void initGui() {
        this.addButton(new ActionButton(1, this.width / 2 - 100, this.height / 2 + 60, 200, 20, "Cancel", () -> {
            Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
        }));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof ActionButton) {
            ((ActionButton) button).onClicked();
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        this.drawDefaultBackground();
        if (isInfo) {
            drawCenteredString(font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            drawCenteredString(font, getMessage(), this.width / 2, this.height / 2 - 24, 0xFFFFFF);
        } else if (getMessage().toLowerCase().contains("no such host is known") || getMessage().toLowerCase().contains("connection reset") || getMessage().toLowerCase().contains("unknownhost")) {
            drawCenteredString(font,  this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            drawCenteredString(font, "The servers could be down or it could be an internet problem.", this.width / 2, this.height / 2 - 28, 0xFFFFFF);
            drawCenteredString(font,  "If you believe this is a bug please create an issue at", this.width / 2, this.height / 2 - 12, 0xFFFFFF);
            drawCenteredString(font,  "https://github.com/Sintinium/oauth with your latest log file.", this.width / 2, this.height / 2, 0xFFFFFF);
        } else {
            String github = "Please create an issue at https://github.com/Sintinium/oauth with your log file.";
            drawCenteredString(font, "An error occurred. This could be a bug.", this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            drawCenteredString(font, github, this.width / 2, this.height / 2 - 28, 0xFFFFFF);
            float scale = .5f;
            GlStateManager.scale(scale, scale, scale);
            String msg = getMessage();
            Iterable<String> messages = Splitter.fixedLength(Math.round(80 * (1f / scale))).limit(12).split(msg);
            int index = 0;
            for (String m : messages) {
                font.drawStringWithShadow(m, this.width / 2f - font.getStringWidth(m) / 2f * scale, (this.height / 2f - 16f) * (1f / scale) + (index * 12f), 0xFF4444);
                index++;
            }
            GlStateManager.scale(1f / scale, 1f / scale, 1f / scale);
        }

        super.drawScreen(p_230430_2_, p_230430_3_, p_230430_4_);
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
}
