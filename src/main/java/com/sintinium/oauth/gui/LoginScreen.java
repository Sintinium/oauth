package com.sintinium.oauth.gui;

import com.sintinium.oauth.OAuthConfig;
import com.sintinium.oauth.login.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class LoginScreen extends GuiScreenCustom {
    private final GuiScreen lastScreen;
    private final GuiMultiplayer multiplayerScreen;
    private ActionButton mojangLoginButton;
    private PasswordFieldWidget passwordWidget;
    private OAuthCheckbox savePasswordWidget;
    private GuiTextField usernameWidget;
    private AtomicReference<String> status = new AtomicReference<>();
    private String title = "OAuth Login";

    private List<Runnable> toRun = new CopyOnWriteArrayList<>();

    public LoginScreen(GuiScreen last, GuiMultiplayer multiplayerScreen) {
        this.lastScreen = last;
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void setWorldAndResolution(Minecraft mcIn, int w, int h) {
        String user = "";
        if (usernameWidget != null) {
            user = usernameWidget.getText();
        }
        String pass = "";
        if (passwordWidget != null) {
            pass = passwordWidget.getText();
        }
        super.setWorldAndResolution(mcIn, w, h);
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

        this.passwordWidget = new PasswordFieldWidget(this.fontRendererObj, this.width / 2 - 100, this.height / 2 - 20, 200, 20);
        this.passwordWidget.setMaxStringLength(128);

        this.usernameWidget = new UsernameFieldWidget(this.fontRendererObj, this.width / 2 - 100, this.height / 2 - 60, 200, 20, passwordWidget);
        this.usernameWidget.setFocused(true);
        if (LoginUtil.lastMojangUsername != null) {
            this.usernameWidget.setText(LoginUtil.lastMojangUsername);
        }

        this.savePasswordWidget = this.addButton(new OAuthCheckbox(4, this.width / 2 - this.fontRendererObj.getStringWidth("Save password") - 25, this.height / 2 + 1 + 2, "Save password", false));

        Runnable savePw = () -> {
            if (savePasswordWidget.isChecked()) {
                saveLoginInfo();
            } else {
                removeLoginInfo();
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
                        toRun.add(() -> Minecraft.getMinecraft().displayGuiScreen(multiplayerScreen));
                    }
                }
            });
            thread.start();
        }, this::updateLoginButton, () -> this.mojangLoginButton.displayString = "Login"));

        this.addButton(new ActionButton(3, this.width / 2 - 100, this.height / 2 + 60, 200, 20, "Cancel", () -> {
            if (!this.savePasswordWidget.isChecked()) {
                removeLoginInfo();
            }
            Minecraft.getMinecraft().displayGuiScreen(lastScreen);
        }));

        this.cleanUp();

        if (OAuthConfig.isSavedPassword()) {
            this.usernameWidget.setText(OAuthConfig.getUsername());
            this.passwordWidget.setText(OAuthConfig.getPassword());
            this.savePasswordWidget.setIsChecked(true);
        }

    }

    private void saveLoginInfo() {
        OAuthConfig.setUsername(usernameWidget.getText());
        OAuthConfig.setPassword(passwordWidget.getText());
    }

    private void removeLoginInfo() {
        OAuthConfig.removeUsernamePassword();
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
        } else if (button instanceof OAuthCheckbox) {
            // Do nothing
        } else {
            throw new RuntimeException("Missing button action");
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        this.usernameWidget.textboxKeyTyped(typedChar, keyCode);
        this.passwordWidget.textboxKeyTyped(typedChar, keyCode);
        this.cleanUp();

        if (keyCode == Keyboard.KEY_TAB) {
            this.usernameWidget.setFocused(!this.passwordWidget.isFocused());
            this.passwordWidget.setFocused(!this.usernameWidget.isFocused());
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        usernameWidget.mouseClicked(mouseX, mouseY, mouseButton);
        passwordWidget.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground(0);
        drawCenteredString(mc.fontRenderer, title, width / 2, 17, 16777215);
        drawString(mc.fontRenderer, "Username/Email", this.width / 2 - 100, this.height / 2 - 60 - 12, 10526880);
        drawString(mc.fontRenderer, "Password", this.width / 2 - 100, this.height / 2 - 20 - 12, 10526880);

        if (status.get() != null) {
            drawCenteredString(mc.fontRenderer, status.get(), width / 2, height / 2 + 20, 0xFF0000);
        }
        this.usernameWidget.drawTextBox();
        this.passwordWidget.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
