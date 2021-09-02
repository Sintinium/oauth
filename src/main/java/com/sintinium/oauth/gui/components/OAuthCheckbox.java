package com.sintinium.oauth.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class OAuthCheckbox extends AbstractButton {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    private static final int TEXT_COLOR = 14737632;
    private final boolean showLabel;
    private final Consumer<Boolean> onChange;
    private boolean selected;

    public OAuthCheckbox(int p_93826_, int p_93827_, int p_93828_, int p_93829_, Component p_93830_, boolean p_93831_) {
        this(p_93826_, p_93827_, p_93828_, p_93829_, p_93830_, p_93831_, true, (selected) -> {
        });
    }

    public OAuthCheckbox(int p_93826_, int p_93827_, int p_93828_, int p_93829_, Component p_93830_, boolean p_93831_, Consumer<Boolean> onChange) {
        this(p_93826_, p_93827_, p_93828_, p_93829_, p_93830_, p_93831_, true, onChange);
    }

    public OAuthCheckbox(int p_93833_, int p_93834_, int p_93835_, int p_93836_, Component p_93837_, boolean p_93838_, boolean p_93839_, Consumer<Boolean> onChange) {
        super(p_93833_, p_93834_, p_93835_, p_93836_, p_93837_);
        this.selected = p_93838_;
        this.showLabel = p_93839_;
        this.onChange = onChange;
    }

    public void onPress() {
        this.selected = !this.selected;
        onChange.accept(this.selected);
    }

    public boolean selected() {
        return this.selected;
    }

    public void updateNarration(NarrationElementOutput p_168846_) {
        p_168846_.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                p_168846_.add(NarratedElementType.USAGE, new TranslatableComponent("narration.checkbox.usage.focused"));
            } else {
                p_168846_.add(NarratedElementType.USAGE, new TranslatableComponent("narration.checkbox.usage.hovered"));
            }
        }

    }

    public void renderButton(PoseStack p_93843_, int p_93844_, int p_93845_, float p_93846_) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableDepthTest();
        Font font = minecraft.font;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        p_93843_.pushPose();
        float scale = .50f;
        float inv = 1 / scale;
        p_93843_.scale(scale, scale, scale);
        blit(p_93843_, (int) (this.x * inv), (int) (this.y * inv), this.isFocused() ? 20.0F : 0.0F, this.selected ? 20.0F : 0.0F, 20, this.height, 64, 64);
        p_93843_.popPose();

        this.renderBg(p_93843_, minecraft, p_93844_, p_93845_);
        if (this.showLabel) {
            drawString(p_93843_, font, this.getMessage(), this.x + (int) (24 * scale) + 2, this.y + (int) (this.height * scale - 8) / 2, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
        }

    }
}
