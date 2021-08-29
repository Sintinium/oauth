package com.sintinium.oauth.gui;

import com.sintinium.oauth.EncryptionUtil;
import com.sintinium.oauth.OAuth;
import com.sintinium.oauth.OAuthConfig;
import com.sintinium.oauth.login.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
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
    private ActionCheckbox savePasswordWidget;
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
        String user = usernameWidget.getText();
        String pass = passwordWidget.getText();
        super.onResize(mcIn, w, h);
        initGui();
        usernameWidget.setText(user);
        passwordWidget.setText(pass);
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
        buttonList.clear();

        this.passwordWidget = new PasswordFieldWidget(0, this.mc.fontRenderer, this.width / 2 - 100, this.height / 2 - 20, 200, 20);
        this.passwordWidget.setMaxStringLength(128);
        this.passwordWidget.setGuiResponder(guiResponder);

        this.usernameWidget = new UsernameFieldWidget(1, this.mc.fontRenderer, this.width / 2 - 100, this.height / 2 - 60, 200, 20, passwordWidget);
        this.usernameWidget.setFocused(true);
        if (LoginUtil.lastMojangUsername != null) {
            this.usernameWidget.setText(LoginUtil.lastMojangUsername);
        }
        this.usernameWidget.setGuiResponder(guiResponder);

        savePasswordWidget = this.addButton(new ActionCheckbox(4, this.width / 2 - fontRenderer.getStringWidth("Save password") - 25, this.height / 2 + 1 + 2, "Save password", OAuth.savePassword, (actionCheckbox, isChecked) -> {
            OAuth.savePassword = !OAuth.savePassword;
        }));

        Runnable savePw = () -> {
            if (OAuth.savePassword) {
                OAuthConfig.lastUsername = usernameWidget.getText();
                OAuthConfig.lastPassword = EncryptionUtil.encryptString(passwordWidget.getText(), Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replaceAll("\\\\", "/"));
                ConfigManager.sync("oauth", Config.Type.INSTANCE);
            } else {
                OAuthConfig.lastUsername = "";
                OAuthConfig.lastPassword = "";
                ConfigManager.sync("oauth", Config.Type.INSTANCE);
            }
        };

        this.mojangLoginButton = this.addButton(new ResponsiveButton(2, this.width / 2 - 100, this.height / 2 + 36, 200, 20, "Login", () -> {
            Thread thread = new Thread(() -> {
                if (usernameWidget.getText().isEmpty()) {
                    toRun.add(() -> this.status.set("Missing username!"));
                } else {
                    Optional<Boolean> didSuccessfullyLogIn = LoginUtil.loginMojangOrLegacy(usernameWidget.getText(), passwordWidget.getText());
                    savePw.run();
                    if (!didSuccessfullyLogIn.isPresent()) {
                        toRun.add(() -> this.status.set("You seem to be offline. Check your connection!"));
                    } else if (!didSuccessfullyLogIn.get()) {
                        toRun.add(() -> this.status.set("Wrong password or username!"));
                    } else {
                        LoginUtil.updateOnlineStatus();
                        toRun.add(() -> {
                            Minecraft.getMinecraft().displayGuiScreen(multiplayerScreen);
                        });
                    }
                }
            });
            thread.start();
        }, this::updateLoginButton, () -> this.mojangLoginButton.displayString = "Login"));

        this.addButton(new ActionButton(3, this.width / 2 - 100, this.height / 2 + 60, 200, 20, "Cancel", () -> {
            savePw.run();
            Minecraft.getMinecraft().displayGuiScreen(lastScreen);
        }));

        if (OAuthConfig.lastUsername.isEmpty() || OAuthConfig.lastPassword.isEmpty()) {
            OAuth.savePassword = false;
        } else {
            OAuth.savePassword = true;
            this.usernameWidget.setText(OAuthConfig.lastUsername);
            this.passwordWidget.setText(EncryptionUtil.decryptString(OAuthConfig.lastPassword, Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replaceAll("\\\\", "/")));
        }

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
        } else if (button instanceof ActionCheckbox) {
            ((ActionCheckbox) button).onClicked();
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
        drawString(mc.fontRenderer, "Username/Email", this.width / 2 - 100, this.height / 2 - 60 - 12, 10526880);
        drawString(mc.fontRenderer, "Password", this.width / 2 - 100, this.height / 2 - 20 - 12, 10526880);

        if (status.get() != null) {
            drawCenteredString(mc.fontRenderer, status.get(), width / 2, height / 2 + 10, 0xFF0000);
        }
        this.usernameWidget.drawTextBox();
        this.passwordWidget.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (mouseX >= savePasswordWidget.x && mouseX < savePasswordWidget.x + savePasswordWidget.width && mouseY >= savePasswordWidget.y && mouseY < savePasswordWidget.y + savePasswordWidget.height) {
            drawHoveringText("This will save your password to a file in your configs. While it will be encrypted if you have a virus someone could extract your password.", mouseX, mouseY);
        }
    }

}
