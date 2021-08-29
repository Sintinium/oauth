package com.sintinium.oauth.gui;

import net.minecraftforge.fml.client.config.GuiCheckBox;

import java.util.function.BiConsumer;

public class ActionCheckbox extends GuiCheckBox {
    private BiConsumer<ActionCheckbox, Boolean> action;

    public ActionCheckbox(int id, int xPos, int yPos, String displayString, boolean isChecked, BiConsumer<ActionCheckbox, Boolean> onStateChange) {
        super(id, xPos, yPos, displayString, isChecked);
        this.action = onStateChange;
    }

    public void onClicked() {
        action.accept(this, this.isChecked());
    }
}
