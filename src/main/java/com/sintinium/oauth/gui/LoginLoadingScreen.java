package com.sintinium.oauth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class LoginLoadingScreen extends Screen {

    private String loadingText = "Loading";
    private int dots = 0;
    private String renderText = loadingText;

    private Screen multiplayerScreen;
    private Screen lastScreen;
    private int tick = 0;
    private Runnable onCancel;
    private boolean isMicrosoft;

    protected LoginLoadingScreen(Screen multiplayerScreen, Screen callingScreen, Runnable onCancel, boolean isMicrosoft) {
        super(new StringTextComponent("Logging in"));
        this.multiplayerScreen = multiplayerScreen;
        this.lastScreen = callingScreen;
        this.onCancel = onCancel;
        this.isMicrosoft = isMicrosoft;
    }

    @Override
    protected void init() {
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20, DialogTexts.GUI_CANCEL, (p_213029_1_) -> {
            onCancel.run();
            Minecraft.getInstance().setScreen(lastScreen);
        }));
    }

    @Override
    public void tick() {
        tick++;
        if (tick % 20 != 0) return;
        dots++;
        if (dots >= 3) {
            dots = 0;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(loadingText);
        for (int i = 0; i < dots; i++) {
            builder.append(".");
        }
        renderText = builder.toString();
    }

    @Override
    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        this.renderBackground(p_230430_1_);
        drawCenteredString(p_230430_1_, Minecraft.getInstance().font, renderText, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        if (this.isMicrosoft) {
            drawCenteredString(p_230430_1_, Minecraft.getInstance().font, "Check your browser", this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        }
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
    }
}
