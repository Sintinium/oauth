package com.sintinium.oauth.gui;

import net.minecraft.client.Minecraft;

public class ResponsiveButton extends ActionButton {
    private Runnable onHover;
    private Runnable onUnhover;
    private boolean wasHovered = false;

    public ResponsiveButton(int buttonId, int x, int y, int width, int height, String text, Runnable onClick, Runnable onHover, Runnable onUnhover) {
        super(buttonId, x, y, width, height, text, onClick);
        this.onHover = onHover;
        this.onUnhover = onUnhover;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        if (!this.enabled) {
            onUnhover.run();
            return;
        }

        if (this.hovered) {
            onHover.run();
        } else {
            onUnhover.run();
        }
    }
}
