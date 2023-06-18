package com.sintinium.oauth.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiUtils {

    public static void drawShadow(GuiGraphics guiGraphics, String text, int x, int y, int color) {
        guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, color);
    }

    public static void drawShadow(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, color);
    }

    public static void drawShadow(GuiGraphics guiGraphics, String text, float x, float y, int color) {
        guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, color, true);
    }

    public static void drawCentered(GuiGraphics guiGraphics, String text, int x, int y, int color) {
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, x, y, color);
    }

    public static void drawCentered(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, x, y, color);
    }

    public static void drawCentered(GuiGraphics guiGraphics, String text, float x, float y, int color) {
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, (int) x, (int) y, color);
    }

}
