package com.sintinium.oauth.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PasswordFieldWidget extends AbstractWidget implements Widget, GuiEventListener {


    public static final int BACKWARDS = -1;
    public static final int FORWARDS = 1;
    public static final int DEFAULT_TEXT_COLOR = 14737632;
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    private static final int BORDER_COLOR_FOCUSED = -1;
    private static final int BORDER_COLOR = -6250336;
    private static final int BACKGROUND_COLOR = -16777216;
    private final Font font;
    private String value;
    private int maxLength;
    private int frame;
    private boolean bordered;
    private boolean canLoseFocus;
    private boolean isEditable;
    private boolean shiftPressed;
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private int textColor;
    private int textColorUneditable;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> responder;
    private Predicate<String> filter;
    private BiFunction<String, Integer, FormattedCharSequence> formatter;

    public PasswordFieldWidget(Font p_94114_, int p_94115_, int p_94116_, int p_94117_, int p_94118_, Component p_94119_) {
        this(p_94114_, p_94115_, p_94116_, p_94117_, p_94118_, (EditBox) null, p_94119_);
    }

    public PasswordFieldWidget(Font p_94106_, int p_94107_, int p_94108_, int p_94109_, int p_94110_, @Nullable EditBox p_94111_, Component p_94112_) {
        super(p_94107_, p_94108_, p_94109_, p_94110_, p_94112_);
        this.value = "";
        this.maxLength = 32;
        this.bordered = true;
        this.canLoseFocus = true;
        this.isEditable = true;
        this.textColor = 14737632;
        this.textColorUneditable = 7368816;
        this.filter = Objects::nonNull;
        this.formatter = (p_94147_, p_94148_) -> {
            return FormattedCharSequence.forward(p_94147_, Style.EMPTY);
        };
        this.font = p_94106_;
        if (p_94111_ != null) {
            this.setValue(p_94111_.getValue());
        }

    }

    public void setResponder(Consumer<String> p_94152_) {
        this.responder = p_94152_;
    }

    public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> p_94150_) {
        this.formatter = p_94150_;
    }

    public void tick() {
        ++this.frame;
    }

    protected MutableComponent createNarrationMessage() {
        Component var1 = this.getMessage();
        return new TranslatableComponent("gui.narrate.editBox", new Object[]{var1, this.value});
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String p_94145_) {
        if (this.filter.test(p_94145_)) {
            if (p_94145_.length() > this.maxLength) {
                this.value = p_94145_.substring(0, this.maxLength);
            } else {
                this.value = p_94145_;
            }

            this.moveCursorToEnd();
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(p_94145_);
        }
    }

    public String getHighlighted() {
        int var1 = Math.min(this.cursorPos, this.highlightPos);
        int var2 = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(var1, var2);
    }

    public void setFilter(Predicate<String> p_94154_) {
        this.filter = p_94154_;
    }

    public void insertText(String p_94165_) {
        int var2 = Math.min(this.cursorPos, this.highlightPos);
        int var3 = Math.max(this.cursorPos, this.highlightPos);
        int var4 = this.maxLength - this.value.length() - (var2 - var3);
        String var5 = SharedConstants.filterText(p_94165_);
        int var6 = var5.length();
        if (var4 < var6) {
            var5 = var5.substring(0, var4);
            var6 = var4;
        }

        String var7 = (new StringBuilder(this.value)).replace(var2, var3, var5).toString();
        if (this.filter.test(var7)) {
            this.value = var7;
            this.setCursorPosition(var2 + var6);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    private void onValueChange(String p_94175_) {
        if (this.responder != null) {
            this.responder.accept(p_94175_);
        }

    }

    private void deleteText(int p_94218_) {
        if (Screen.hasControlDown()) {
            this.deleteWords(p_94218_);
        } else {
            this.deleteChars(p_94218_);
        }

    }

    public void deleteWords(int p_94177_) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(p_94177_) - this.cursorPos);
            }
        }
    }

    public void deleteChars(int p_94181_) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int var2 = this.getCursorPos(p_94181_);
                int var3 = Math.min(var2, this.cursorPos);
                int var4 = Math.max(var2, this.cursorPos);
                if (var3 != var4) {
                    String var5 = (new StringBuilder(this.value)).delete(var3, var4).toString();
                    if (this.filter.test(var5)) {
                        this.value = var5;
                        this.moveCursorTo(var3);
                    }
                }
            }
        }
    }

    public int getWordPosition(int p_94185_) {
        return this.getWordPosition(p_94185_, this.getCursorPosition());
    }

    private int getWordPosition(int p_94129_, int p_94130_) {
        return this.getWordPosition(p_94129_, p_94130_, true);
    }

    private int getWordPosition(int p_94141_, int p_94142_, boolean p_94143_) {
        int var4 = p_94142_;
        boolean var5 = p_94141_ < 0;
        int var6 = Math.abs(p_94141_);

        for (int var7 = 0; var7 < var6; ++var7) {
            if (!var5) {
                int var8 = this.value.length();
                var4 = this.value.indexOf(32, var4);
                if (var4 == -1) {
                    var4 = var8;
                } else {
                    while (p_94143_ && var4 < var8 && this.value.charAt(var4) == ' ') {
                        ++var4;
                    }
                }
            } else {
                while (p_94143_ && var4 > 0 && this.value.charAt(var4 - 1) == ' ') {
                    --var4;
                }

                while (var4 > 0 && this.value.charAt(var4 - 1) != ' ') {
                    --var4;
                }
            }
        }

        return var4;
    }

    public void moveCursor(int p_94189_) {
        this.moveCursorTo(this.getCursorPos(p_94189_));
    }

    private int getCursorPos(int p_94221_) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, p_94221_);
    }

    public void moveCursorTo(int p_94193_) {
        this.setCursorPosition(p_94193_);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    public boolean keyPressed(int p_94132_, int p_94133_, int p_94134_) {
        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(p_94132_)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(p_94132_)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (Screen.isPaste(p_94132_)) {
                if (this.isEditable) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(p_94132_)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                if (this.isEditable) {
                    this.insertText("");
                }

                return true;
            } else {
                switch (p_94132_) {
                    case 259:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(-1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.moveCursorToStart();
                        return true;
                    case 269:
                        this.moveCursorToEnd();
                        return true;
                }
            }
        }
    }

    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    public boolean charTyped(char p_94122_, int p_94123_) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (SharedConstants.isAllowedChatCharacter(p_94122_)) {
            if (this.isEditable) {
                this.insertText(Character.toString(p_94122_));
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    public boolean mouseClicked(double p_94125_, double p_94126_, int p_94127_) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean var6 = p_94125_ >= (double) this.x && p_94125_ < (double) (this.x + this.width) && p_94126_ >= (double) this.y && p_94126_ < (double) (this.y + this.height);
            if (this.canLoseFocus) {
                this.setFocus(var6);
            }

            if (this.isFocused() && var6 && p_94127_ == 0) {
                int var7 = Mth.floor(p_94125_) - this.x;
                if (this.bordered) {
                    var7 -= 4;
                }

                String var8 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
                this.moveCursorTo(this.font.plainSubstrByWidth(var8, var7).length() + this.displayPos);
                return true;
            } else {
                return false;
            }
        }
    }

    public void setFocus(boolean p_94179_) {
        this.setFocused(p_94179_);
    }

    // Edit
    private String getHiddenValue() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.getValue().length(); i++) {
//            builder.append("\u2022");
            builder.append("*");
        }
        return builder.toString();
    }

    public void renderButton(PoseStack p_94160_, int p_94161_, int p_94162_, float p_94163_) {
        if (this.isVisible()) {
            int var5;
            if (this.isBordered()) {
                var5 = this.isFocused() ? -1 : -6250336;
                fill(p_94160_, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, var5);
                fill(p_94160_, this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
            }

            var5 = this.isEditable ? this.textColor : this.textColorUneditable;
            int var6 = this.cursorPos - this.displayPos;
            int var7 = this.highlightPos - this.displayPos;
            // edit
//            String var8 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            String var8 = this.font.plainSubstrByWidth(this.getHiddenValue().substring(this.displayPos), this.getInnerWidth());
//            String var8 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean var9 = var6 >= 0 && var6 <= var8.length();
            boolean var10 = this.isFocused() && this.frame / 6 % 2 == 0 && var9;
            int var11 = this.bordered ? this.x + 4 : this.x;
            int var12 = this.bordered ? this.y + (this.height - 8) / 2 : this.y;
            int var13 = var11;
            if (var7 > var8.length()) {
                var7 = var8.length();
            }

            // edit
            p_94160_.pushPose();
            p_94160_.scale(1.5f, 1.5f, 1.5f);
            var11 /= 1.5;
            var12 /= 1.5;
            //

            if (!var8.isEmpty()) {
                String var14 = var9 ? var8.substring(0, var6) : var8;
                var13 = this.font.drawShadow(p_94160_, (FormattedCharSequence) this.formatter.apply(var14, this.displayPos), (float) var11, (float) var12 + 1, var5);
            }

            boolean var17 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int var15 = var13;
            if (!var9) {
                var15 = var6 > 0 ? var11 + this.width : var11;
            } else if (var17) {
                var15 = var13 - 1;
                --var13;
            }

            if (!var8.isEmpty() && var9 && var6 < var8.length()) {
                this.font.drawShadow(p_94160_, (FormattedCharSequence) this.formatter.apply(var8.substring(var6), this.cursorPos), (float) var13, (float) var12 + 1, var5);
            }

            if (!var17 && this.suggestion != null) {
                this.font.drawShadow(p_94160_, this.suggestion, (float) (var15 - 1), (float) var12, -8355712);
            }

            // edit
            p_94160_.popPose();
            var11 *= 1.5;
            var12 *= 1.5;
            var15 *= 1.5;
            if (var8.isEmpty()) {
                var15 /= 1.5;
            }
            //

            int var10002;
            int var10003;
            int var10004;
            if (var10) {
                if (var17) {
                    var10002 = var12 - 1;
                    var10003 = var15 + 1;
                    var10004 = var12 + 1;
                    Objects.requireNonNull(this.font);
                    GuiComponent.fill(p_94160_, var15, var10002, var10003, var10004 + 9, -3092272);
                } else {
                    this.font.drawShadow(p_94160_, "_", (float) var15, (float) var12, var5);
                }
            }

            // edit
            p_94160_.pushPose();
            p_94160_.scale(1.5f, 1.5f, 1.5f);
            var11 /= 1.5;
            var12 /= 1.5;
            var15 /= 1.5;
            //

            if (var7 != var6) {
                int var16 = var11 + this.font.width(var8.substring(0, var7));
                this.renderHighlight(var15, var12 - 1, var16 + 1, var12 + 9);
            }

        }

        // edit
        p_94160_.popPose();
    }
    //

    private void renderHighlight(double p_94136_, double p_94137_, double p_94138_, double p_94139_) {
        double var5;
        if (p_94136_ < p_94138_) {
            var5 = p_94136_;
            p_94136_ = p_94138_;
            p_94138_ = var5;
        }

        if (p_94137_ < p_94139_) {
            var5 = p_94137_;
            p_94137_ = p_94139_;
            p_94139_ = var5;
        }

        if (p_94138_ > this.x + this.width) {
            p_94138_ = this.x + this.width;
        }

        if (p_94136_ > this.x + this.width) {
            p_94136_ = this.x + this.width;
        }

        Tesselator var7 = Tesselator.getInstance();
        BufferBuilder var6 = var7.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        var6.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        var6.vertex((double) p_94136_ * 1.5, (double) p_94139_ * 1.5, 0.0D).endVertex();
        var6.vertex((double) p_94138_ * 1.5, (double) p_94139_ * 1.5, 0.0D).endVertex();
        var6.vertex((double) p_94138_ * 1.5, (double) p_94137_ * 1.5, 0.0D).endVertex();
        var6.vertex((double) p_94136_ * 1.5, (double) p_94137_ * 1.5, 0.0D).endVertex();
        var7.end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public void setMaxLength(int p_94200_) {
        this.maxLength = p_94200_;
        if (this.value.length() > p_94200_) {
            this.value = this.value.substring(0, p_94200_);
            this.onValueChange(this.value);
        }

    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    public void setCursorPosition(int p_94197_) {
        this.cursorPos = Mth.clamp(p_94197_, 0, this.value.length());
    }

    private boolean isBordered() {
        return this.bordered;
    }

    public void setBordered(boolean p_94183_) {
        this.bordered = p_94183_;
    }

    public void setTextColor(int p_94203_) {
        this.textColor = p_94203_;
    }

    public void setTextColorUneditable(int p_94206_) {
        this.textColorUneditable = p_94206_;
    }

    public boolean changeFocus(boolean p_94172_) {
        return this.visible && this.isEditable ? super.changeFocus(p_94172_) : false;
    }

    public boolean isMouseOver(double p_94157_, double p_94158_) {
        return this.visible && p_94157_ >= (double) this.x && p_94157_ < (double) (this.x + this.width) && p_94158_ >= (double) this.y && p_94158_ < (double) (this.y + this.height);
    }

    protected void onFocusedChanged(boolean p_94170_) {
        if (p_94170_) {
            this.frame = 0;
        }

    }

    private boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean p_94187_) {
        this.isEditable = p_94187_;
    }

    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    public void setHighlightPos(int p_94209_) {
        int var2 = this.value.length();
        this.highlightPos = Mth.clamp(p_94209_, 0, var2);
        if (this.font != null) {
            if (this.displayPos > var2) {
                this.displayPos = var2;
            }

            int var3 = this.getInnerWidth();
            String var4 = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), var3);
            int var5 = var4.length() + this.displayPos;
            if (this.highlightPos == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, var3, true).length();
            }

            if (this.highlightPos > var5) {
                this.displayPos += this.highlightPos - var5;
            } else if (this.highlightPos <= this.displayPos) {
                this.displayPos -= this.displayPos - this.highlightPos;
            }

            this.displayPos = Mth.clamp(this.displayPos, 0, var2);
        }

    }

    public void setCanLoseFocus(boolean p_94191_) {
        this.canLoseFocus = p_94191_;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean p_94195_) {
        this.visible = p_94195_;
    }

    public void setSuggestion(@Nullable String p_94168_) {
        this.suggestion = p_94168_;
    }

    public int getScreenX(int p_94212_) {
        return p_94212_ > this.value.length() ? this.x : this.x + this.font.width(this.value.substring(0, p_94212_));
    }

    public void setX(int p_94215_) {
        this.x = p_94215_;
    }

    public void updateNarration(NarrationElementOutput p_169009_) {
        p_169009_.add(NarratedElementType.TITLE, new TranslatableComponent("narration.edit_box", new Object[]{this.getValue()}));
    }

}
