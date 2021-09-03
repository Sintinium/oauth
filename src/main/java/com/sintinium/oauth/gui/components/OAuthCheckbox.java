package com.sintinium.oauth.gui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OAuthCheckbox extends AbstractButton {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    private final boolean showLabel;
    private boolean selected;

    public OAuthCheckbox(int p_i232257_1_, int p_i232257_2_, int p_i232257_3_, int p_i232257_4_, ITextComponent p_i232257_5_, boolean p_i232257_6_) {
        this(p_i232257_1_, p_i232257_2_, p_i232257_3_, p_i232257_4_, p_i232257_5_, p_i232257_6_, true);
    }

    public OAuthCheckbox(int p_i232258_1_, int p_i232258_2_, int p_i232258_3_, int p_i232258_4_, ITextComponent p_i232258_5_, boolean p_i232258_6_, boolean p_i232258_7_) {
        super(p_i232258_1_, p_i232258_2_, p_i232258_3_, p_i232258_4_, p_i232258_5_);
        this.selected = p_i232258_6_;
        this.showLabel = p_i232258_7_;
    }

    public void onPress() {
        this.selected = !this.selected;
    }

    public boolean selected() {
        return this.selected;
    }

    public void renderButton(MatrixStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(TEXTURE);
        RenderSystem.enableDepthTest();
        FontRenderer fontrenderer = minecraft.font;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        stack.pushPose();
        float scale = .50f;
        float inv = 1 / scale;
        stack.scale(scale, scale, scale);
        blit(stack, (int) (this.x * inv), (int) (this.y * inv), this.isFocused() ? 20.0F : 0.0F, this.selected ? 20.0F : 0.0F, 20, this.height, 64, 64);
        stack.popPose();

        this.renderBg(stack, minecraft, p_230431_2_, p_230431_3_);
        if (this.showLabel) {
            drawString(stack, fontrenderer, this.getMessage(), this.x + (int) (24 * scale), this.y + (int) (this.height * scale - 8) / 2, 14737632 | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }
}
