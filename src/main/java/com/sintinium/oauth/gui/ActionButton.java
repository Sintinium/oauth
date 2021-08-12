package com.sintinium.oauth.gui;

import net.minecraft.client.gui.GuiButton;

public class ActionButton extends GuiButton {

    private Runnable onClicked;

    public ActionButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Runnable onClicked) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.onClicked = onClicked;
    }

    public void onClicked() {
        onClicked.run();
    }
}
