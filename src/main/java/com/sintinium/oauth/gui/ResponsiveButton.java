package com.sintinium.oauth.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ResponsiveButton extends Button {
    private final Runnable onHover;
    private final Runnable onUnhover;
    private final boolean wasHovered = false;

    public ResponsiveButton(int p_i232255_1_, int p_i232255_2_, int p_i232255_3_, int p_i232255_4_, Component p_i232255_5_, OnPress p_i232255_6_, Runnable onHover, Runnable onUnhover) {
        super(p_i232255_1_, p_i232255_2_, p_i232255_3_, p_i232255_4_, p_i232255_5_, p_i232255_6_, DEFAULT_NARRATION);
        this.onHover = onHover;
        this.onUnhover = onUnhover;
    }

    @Override
    public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        if (!this.active) {
            onUnhover.run();
            return;
        }
        if (this.isHovered) {
            onHover.run();
        } else {
            onUnhover.run();
        }
    }
}
