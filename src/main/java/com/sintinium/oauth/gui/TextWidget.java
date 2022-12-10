package com.sintinium.oauth.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TextWidget extends Button {
    private String text;

    public TextWidget(int x, int y, int width, int height, String text) {
        super(x, y, width, height, Component.literal(text), p_onPress_1_ -> {
        }, DEFAULT_NARRATION);
    }

    @Override
    public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
        drawString(p_230431_1_, Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
