package com.sintinium.oauth.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;

import java.util.concurrent.atomic.AtomicReference;

public class LoginLoadingScreen extends OAuthScreen {

    private String loadingText = "Loading";
    private int dots = 0;
    private String renderText = loadingText;

    private Screen multiplayerScreen;
    private Screen lastScreen;
    private int tick = 0;
    private Runnable onCancel;
    private boolean isMicrosoft;
    private AtomicReference<String> updateText = new AtomicReference<>();

    protected LoginLoadingScreen(Screen multiplayerScreen, Screen callingScreen, Runnable onCancel, boolean isMicrosoft) {
        super(new TextComponent("Logging in"));
        this.multiplayerScreen = multiplayerScreen;
        this.lastScreen = callingScreen;
        this.onCancel = onCancel;
        this.isMicrosoft = isMicrosoft;

        if (this.isMicrosoft) {
            updateText.set("Check your browser");
        } else {
            updateText.set("Authorizing you with Mojang");
        }
    }

    public void updateText(String text) {
        updateText.set(text);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + 60, 200, 20, CommonComponents.GUI_CANCEL, (p_213029_1_) -> {
            onCancel.run();
            Minecraft.getInstance().setScreen(lastScreen);
        }));
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
        if (tick % 20 != 0) return;
        dots++;
        if (dots > 3) {
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
    public void render(PoseStack matrix, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        this.renderBackground(matrix);
        drawCenteredString(matrix, Minecraft.getInstance().font, renderText, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        if (this.isMicrosoft) {
            drawCenteredString(matrix, Minecraft.getInstance().font, updateText.get(), this.width / 2, this.height / 2 - 28, 0xFFFFFF);
        }
        super.render(matrix, p_230430_2_, p_230430_3_, p_230430_4_);
    }
}
