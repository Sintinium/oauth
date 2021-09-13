package com.sintinium.oauth.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class UsernameFieldWidget extends GuiTextField {

    private PasswordFieldWidget passwordFieldWidget;

    public UsernameFieldWidget(FontRenderer fontRendererObj, int x, int y, int par5Width, int par6Height, PasswordFieldWidget passwordFieldWidget) {
        super(fontRendererObj, x, y, par5Width, par6Height);
        this.passwordFieldWidget = passwordFieldWidget;
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
