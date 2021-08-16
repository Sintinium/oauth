package com.sintinium.oauth.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

public class UsernameFieldWidget extends EditBox {

    private PasswordFieldWidget passwordFieldWidget;

    public UsernameFieldWidget(Font p_i232260_1_, int p_i232260_2_, int p_i232260_3_, int p_i232260_4_, int p_i232260_5_, TextComponent p_i232260_6_, PasswordFieldWidget passwordFieldWidget) {
        super(p_i232260_1_, p_i232260_2_, p_i232260_3_, p_i232260_4_, p_i232260_5_, p_i232260_6_);
        this.passwordFieldWidget = passwordFieldWidget;
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        boolean result = super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
        if (this.isFocused()) {
            passwordFieldWidget.setFocus(false);
        }
        return result;
    }
}
