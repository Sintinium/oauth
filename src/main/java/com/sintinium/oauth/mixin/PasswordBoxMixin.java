package com.sintinium.oauth.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.sintinium.oauth.gui.components.PasswordBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

@Mixin(EditBox.class)
public abstract class PasswordBoxMixin extends AbstractWidget {

    @Shadow
    private boolean isEditable;
    @Shadow
    private int textColor;
    @Shadow
    private int textColorUneditable;
    @Shadow
    private int cursorPos;
    @Shadow
    private int displayPos;
    @Shadow
    private int highlightPos;
    @Shadow
    @Final
    private Font font;
    @Shadow
    private String value;
    @Shadow
    private int frame;
    @Shadow
    private boolean bordered;
    @Shadow
    private BiFunction<String, Integer, FormattedCharSequence> formatter;
    @Shadow
    @Nullable
    private String suggestion;

    public PasswordBoxMixin(int p_93629_, int p_93630_, int p_93631_, int p_93632_, Component p_93633_) {
        super(p_93629_, p_93630_, p_93631_, p_93632_, p_93633_);
    }

    private static String getHiddenValue(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            builder.append("*");
        }
        return builder.toString();
    }

    @Shadow
    public abstract boolean isVisible();

    @Shadow
    protected abstract boolean isBordered();

    @Shadow
    public abstract int getInnerWidth();

    @Shadow
    protected abstract int getMaxLength();

    @Shadow
    protected abstract void renderHighlight(int p_94136_, int p_94137_, int p_94138_, int p_94139_);

    @Inject(method = "renderButton", at = @At("HEAD"), cancellable = true)
    public void onRender(PoseStack pose, int p_94161_, int p_94162_, float p_94163_, CallbackInfo ci) {
        EditBox instance = (EditBox) (Object) this;
        if (instance instanceof PasswordBox) ci.cancel();
        else return;

        if (this.isVisible()) {
            if (this.isBordered()) {
                int i = this.isFocused() ? -1 : -6250336;
                fill(pose, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, i);
                fill(pose, this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
            }

            int i2 = this.isEditable ? this.textColor : this.textColorUneditable;
            int j = this.cursorPos - this.displayPos;
            int k = this.highlightPos - this.displayPos;
            String s = this.font.plainSubstrByWidth(getHiddenValue(this.value).substring(this.displayPos), this.getInnerWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
            int l = this.bordered ? this.x + 4 : this.x;
            int i1 = this.bordered ? this.y + (this.height - 8) / 2 : this.y;
            int j1 = l;
            if (k > s.length()) {
                k = s.length();
            }

            // Added
            pose.pushPose();
            pose.scale(1.5f, 1.5f, 1.5f);
            pose.translate(0f, 0f, 0f);
            l /= 1.5;
            i1 /= 1.5;
            // End

            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 = this.font.drawShadow(pose, this.formatter.apply(s1, this.displayPos), (float) l, (float) i1, i2);
            }

            boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!s.isEmpty() && flag && j < s.length()) {
                this.font.drawShadow(pose, this.formatter.apply(s.substring(j), this.cursorPos), (float) j1, (float) i1, i2);
            }

            if (!flag2 && this.suggestion != null) {
                this.font.drawShadow(pose, this.suggestion, (float) (k1 - 1), (float) i1, -8355712);
            }

            // Added
            pose.popPose();
            l *= 1.5;
            i1 *= 1.5;
            k1 *= 1.5;
            if (s.isEmpty()) {
                k1 /= 1.5;
            }
            // End

            if (flag1) {
                if (flag2) {
                    GuiComponent.fill(pose, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
                } else {
                    this.font.drawShadow(pose, "_", (float) k1, (float) i1, i2);
                }
            }

            // Added
            pose.pushPose();
            pose.scale(1.5f, 1.5f, 1.5f);
            l /= 1.5;
            i1 /= 1.5;
            k1 /= 1.5;
            // End

            if (k != j) {
                int l1 = l + this.font.width(s.substring(0, k));
                this.renderHighlight(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
            }

            // Added
            pose.popPose();
            //End
        }
    }

    @Inject(method = "renderHighlight", at = @At("HEAD"), cancellable = true)
    public void onRenderHighlight(int p_94136_, int p_94137_, int p_94138_, int p_94139_, CallbackInfo ci) {
        EditBox instance = (EditBox) (Object) this;
        if (instance instanceof PasswordBox) ci.cancel();
        else return;


        if (p_94136_ < p_94138_) {
            int i = p_94136_;
            p_94136_ = p_94138_;
            p_94138_ = i;
        }

        if (p_94137_ < p_94139_) {
            int j = p_94137_;
            p_94137_ = p_94139_;
            p_94139_ = j;
        }

        if (p_94138_ > this.x + this.width) {
            p_94138_ = this.x + this.width;
        }

        if (p_94136_ > this.x + this.width) {
            p_94136_ = this.x + this.width;
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);

        // Added
        p_94136_ *= 1.5;
        p_94138_ *= 1.5;
        p_94139_ *= 1.5;
        p_94137_ *= 1.5;
        // End

        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(p_94136_, p_94139_, 0.0D).endVertex();
        bufferbuilder.vertex(p_94138_, p_94139_, 0.0D).endVertex();
        bufferbuilder.vertex(p_94138_, p_94137_, 0.0D).endVertex();
        bufferbuilder.vertex(p_94136_, p_94137_, 0.0D).endVertex();
        tesselator.end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }
}
