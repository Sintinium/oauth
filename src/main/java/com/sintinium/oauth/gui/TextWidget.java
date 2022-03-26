package com.sintinium.oauth.gui;

import net.minecraft.client.gui.GuiScreen;

public class TextWidget {
    private final int x;
    private final int y;
    private String text;
    private int color = 0xFFFFFF;

    public TextWidget(int x, int y, String text) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void draw(GuiScreen screen) {
        screen.drawString(screen.mc.fontRendererObj, text, x, y, color);
    }
}
