package com.sintinium.oauth.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class UsernameFieldWidget extends GuiTextField {

    private PasswordFieldWidget passwordFieldWidget;

    public UsernameFieldWidget(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height, PasswordFieldWidget passwordFieldWidget) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
        this.passwordFieldWidget = passwordFieldWidget;
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isFocused()) {
            passwordFieldWidget.setFocused(false);
        }
    }
}
