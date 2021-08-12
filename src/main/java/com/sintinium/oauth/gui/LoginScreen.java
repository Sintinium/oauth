package com.sintinium.oauth.gui;

import com.sintinium.oauth.login.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class LoginScreen extends GuiScreen {
    private final GuiScreen lastScreen;
    private final GuiMultiplayer multiplayerScreen;
    private ActionButton mojangLoginButton;
    private PasswordFieldWidget passwordWidget;
    private GuiTextField usernameWidget;
    private AtomicReference<String> status = new AtomicReference<>();
    private String title = "OAuth Login";

    private GuiPageButtonList.GuiResponder guiResponder = new GuiPageButtonList.GuiResponder() {
        @Override
        public void setEntryValue(int id, boolean value) {
            onEdited(id, String.valueOf(value));
        }

        @Override
        public void setEntryValue(int id, float value) {
            onEdited(id, String.valueOf(value));
        }

        @Override
        public void setEntryValue(int id, String value) {
            onEdited(id, value);
        }
    };

    private List<Runnable> toRun = new CopyOnWriteArrayList<>();

    public LoginScreen(GuiScreen last, GuiMultiplayer multiplayerScreen) {
        this.lastScreen = last;
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {

    }

    @Override
    public void updateScreen() {
        this.usernameWidget.updateCursorCounter();
        this.passwordWidget.updateCursorCounter();
        if (!toRun.isEmpty()) {
            for (Runnable r : toRun) {
                r.run();
            }
            toRun.clear();
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        this.passwordWidget = new PasswordFieldWidget(0, this.mc.fontRenderer, this.width / 2 - 100, 106, 200, 20);
        this.passwordWidget.setMaxStringLength(128);
        this.passwordWidget.setGuiResponder(guiResponder);

        this.usernameWidget = new UsernameFieldWidget(1, this.mc.fontRenderer, this.width / 2 - 100, 66, 200, 20, passwordWidget);
        this.usernameWidget.setFocused(true);
        if (LoginUtil.lastMojangUsername != null) {
            this.usernameWidget.setText(LoginUtil.lastMojangUsername);
        }
        this.usernameWidget.setGuiResponder(guiResponder);


        this.mojangLoginButton = this.addButton(new ResponsiveButton(2, this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20, "Login", () -> {
            Thread thread = new Thread(() -> {
                if (usernameWidget.getText().isEmpty()) {
                    toRun.add(() -> this.status.set("Missing username!"));
                } else {
                    Optional<Boolean> didSuccessfullyLogIn = LoginUtil.loginMojangOrLegacy(usernameWidget.getText(), passwordWidget.getText());
                    if (!didSuccessfullyLogIn.isPresent()) {
                        toRun.add(() -> this.status.set("You seem to be offline. Check your connection!"));
                    } else if (!didSuccessfullyLogIn.get()) {
                        toRun.add(() -> this.status.set("Wrong password or username!"));
                    } else {
                        LoginUtil.updateOnlineStatus();
                        toRun.add(() -> Minecraft.getMinecraft().displayGuiScreen(multiplayerScreen));
                    }
                }
            });
            thread.start();
        }, this::updateLoginButton, () -> this.mojangLoginButton.displayString = "Login"));

        this.addButton(new ActionButton(3, this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20, "Cancel", () -> {
            Minecraft.getMinecraft().displayGuiScreen(lastScreen);
        }));
        this.cleanUp();
    }

    private void onEdited(int id, String value) {
        this.cleanUp();
    }

    private void updateLoginButton() {
        if (this.passwordWidget.getText().isEmpty()) {
            this.mojangLoginButton.displayString = "Login Offline";
        } else {
            this.mojangLoginButton.displayString = "Login";
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void cleanUp() {
        this.mojangLoginButton.enabled = !this.usernameWidget.getText().isEmpty();
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
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.usernameWidget.textboxKeyTyped(typedChar, keyCode);
        this.passwordWidget.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_TAB) {
            this.usernameWidget.setFocused(!this.passwordWidget.isFocused());
            this.passwordWidget.setFocused(!this.usernameWidget.isFocused());
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        usernameWidget.mouseClicked(mouseX, mouseY, mouseButton);
        passwordWidget.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground(0);
        drawCenteredString(mc.fontRenderer, title, width / 2, 17, 16777215);
        drawString(mc.fontRenderer, "Username/Email", this.width / 2 - 100, 53, 10526880);
        drawString(mc.fontRenderer, "Password", this.width / 2 - 100, 94, 10526880);

        if (status.get() != null) {
            drawCenteredString(mc.fontRenderer, status.get(), width / 2, height / 2 + 10, 0xFF0000);
        }
        this.usernameWidget.drawTextBox();
        this.passwordWidget.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
