package com.sintinium.oauth.gui;

import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiScreenCustom extends GuiScreen {
    private static final AtomicReference<GuiScreen> screenToSet = new AtomicReference<>(null);

    @SuppressWarnings("unchecked")
	protected <T extends GuiButton> T addButton(T buttonIn) {
        this.buttonList.add(buttonIn);
        return buttonIn;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof ActionButton) {
            ((ActionButton) button).onClicked();
        } else {
            throw new RuntimeException("Missing button action");
        }
    }

    /**
     * Safely sets screen even if called in a different thread.
     */
    public static void setScreen(GuiScreen screen) {
        Thread thr = Thread.currentThread();
        if ((thr.getName().equals("Client thread"))) {
            Minecraft.getMinecraft().displayGuiScreen(screen);
            return;
        }
        screenToSet.set(screen);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        GuiScreen screenToSet = GuiScreenCustom.screenToSet.getAndSet(null);
        if (screenToSet != null) {
            Minecraft.getMinecraft().displayGuiScreen(screenToSet);
        }
    }

    public static boolean isAltKeyDown() {
        return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
    }

    public static boolean isKeyComboCtrlX(int keyID) {
        return keyID == 45 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public static boolean isKeyComboCtrlV(int keyID) {
        return keyID == 47 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public static boolean isKeyComboCtrlC(int keyID) {
        return keyID == 46 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public static boolean isKeyComboCtrlA(int keyID) {
        return keyID == 30 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }
}
