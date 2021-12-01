package com.sintinium.oauth.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

public class TextWidget extends Button {
    private String text;

    public TextWidget(int x, int y, int width, int height, String text) {
        super(x, y, width, height, new TextComponent(text), p_onPress_1_ -> {
        });
    }

    @Override
    public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
        drawString(p_230431_1_, Minecraft.getInstance().font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
