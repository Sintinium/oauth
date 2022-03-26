package com.sintinium.oauth.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.concurrent.atomic.AtomicReference;

public class LoginLoadingScreen extends GuiScreen {

    private String loadingText = "Loading";
    private int dots = 0;
    private String renderText = loadingText;

    private GuiScreen multiplayerScreen;
    private GuiScreen lastScreen;
    private int tick = 0;
    private Runnable onCancel;
    private boolean isMicrosoft;
    private String title = "Logging in";
    private AtomicReference<String> updateText = new AtomicReference<>();

    protected LoginLoadingScreen(GuiScreen multiplayerScreen, GuiScreen callingScreen, Runnable onCancel, boolean isMicrosoft) {
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
    public void initGui() {
        this.addButton(new ActionButton(0, this.width / 2 - 100, this.height / 2 + 60, 200, 20, "Cancel", () -> {
            onCancel.run();
            Minecraft.getMinecraft().displayGuiScreen(lastScreen);
        }));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof ActionButton) {
            ((ActionButton) button).onClicked();
        } else {
            throw new RuntimeException("Missing button action");
        }
    }

    @Override
    public void updateScreen() {
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground(0);
        drawCenteredString(Minecraft.getMinecraft().fontRenderer, renderText, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        if (this.isMicrosoft) {
            drawCenteredString(Minecraft.getMinecraft().fontRenderer, updateText.get(), this.width / 2, this.height / 2 - 28, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
