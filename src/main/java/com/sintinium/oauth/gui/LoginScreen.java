package com.sintinium.oauth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.sintinium.oauth.OAuth;
import com.sintinium.oauth.gui.components.OAuthCheckbox;
import com.sintinium.oauth.login.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class LoginScreen extends Screen {
    private final Screen lastScreen;
    private final MultiplayerScreen multiplayerScreen;
    private Button mojangLoginButton;
    private PasswordFieldWidget passwordWidget;
    private TextFieldWidget usernameWidget;
    private OAuthCheckbox savePasswordButton;
    private AtomicReference<String> status = new AtomicReference<>();

    private List<Runnable> toRun = new CopyOnWriteArrayList<>();

    public LoginScreen(Screen last, MultiplayerScreen multiplayerScreen) {
        super(new StringTextComponent("OAuth Login"));
        this.lastScreen = last;
        this.multiplayerScreen = multiplayerScreen;
    }

    public void tick() {
        this.usernameWidget.tick();
        this.passwordWidget.tick();
        if (!toRun.isEmpty()) {
            for (Runnable r : toRun) {
                r.run();
            }
            toRun.clear();
        }
//        OAuth.savePassword = this.savePasswordButton.selected();
    }

    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);

        this.passwordWidget = new PasswordFieldWidget(this.font, this.width / 2 - 100, this.height / 2 - 20, 200, 20, new StringTextComponent("Password"));
        this.passwordWidget.setMaxLength(128);
        this.passwordWidget.setResponder(this::onEdited);

        this.usernameWidget = new UsernameFieldWidget(this.font, this.width / 2 - 100, this.height / 2 - 60, 200, 20, new StringTextComponent("Username/Email"), passwordWidget);
        this.usernameWidget.setFocus(true);
        if (LoginUtil.lastMojangUsername != null) {
            this.usernameWidget.setValue(LoginUtil.lastMojangUsername);
        }
        this.usernameWidget.setResponder(this::onEdited);

        this.children.add(this.usernameWidget);
        this.children.add(this.passwordWidget);

        this.savePasswordButton = this.addButton(new OAuthCheckbox(this.width / 2 - 101, this.height / 2 + 3, 150, 20, new StringTextComponent("Save Password"), false));

        this.mojangLoginButton = this.addButton(new ResponsiveButton(this.width / 2 - 100, this.height / 2 + 36, 200, 20, new StringTextComponent("Login"), (p_213030_1_) -> {
            Thread thread = new Thread(() -> {
                if (usernameWidget.getValue().isEmpty()) {
                    toRun.add(() -> this.status.set("Missing username!"));
                } else {
                    Optional<Boolean> didSuccessfullyLogIn = LoginUtil.loginMojangOrLegacy(usernameWidget.getValue(), passwordWidget.getValue());
                    if (!didSuccessfullyLogIn.isPresent()) {
                        toRun.add(() -> this.status.set("You seem to be offline. Check your connection!"));
                    } else if (!didSuccessfullyLogIn.get()) {
                        toRun.add(() -> this.status.set("Wrong password or username!"));
                        if (this.savePasswordButton.selected()) {
                            saveLoginInfo();
                        } else {
                            removeLoginInfo();
                        }
                    } else {
                        LoginUtil.updateOnlineStatus();
                        toRun.add(() -> Minecraft.getInstance().setScreen(multiplayerScreen));
                        if (this.savePasswordButton.selected()) {
                            saveLoginInfo();
                        } else {
                            removeLoginInfo();
                        }
                    }
                }
            });
            thread.start();
        }, this::updateLoginButton, () -> this.mojangLoginButton.setMessage(new StringTextComponent("Login"))));

        this.addButton(new Button(this.width / 2 - 100, this.height / 2 + 60, 200, 20, DialogTexts.GUI_CANCEL, (p_213029_1_) -> {
            Minecraft.getInstance().setScreen(lastScreen);
            if (!this.savePasswordButton.selected()) {
                removeLoginInfo();
            }
        }));

        this.cleanUp();

        if (OAuth.getInstance().config.isSavedPassword()) {
            this.usernameWidget.setValue(OAuth.getInstance().config.getUsername());
            this.passwordWidget.setValue(OAuth.getInstance().config.getPassword());
            this.savePasswordButton.onPress();
        }
    }

    private void saveLoginInfo() {
        OAuth.getInstance().config.setUsername(usernameWidget.getValue());
        OAuth.getInstance().config.setPassword(passwordWidget.getValue());
    }

    private void removeLoginInfo() {
        OAuth.getInstance().config.removeUsernamePassword();
    }

    public void resize(Minecraft p_231152_1_, int p_231152_2_, int p_231152_3_) {
        String s = this.passwordWidget.getValue();
        String s1 = this.usernameWidget.getValue();
        this.init(p_231152_1_, p_231152_2_, p_231152_3_);
        this.passwordWidget.setValue(s);
        this.usernameWidget.setValue(s1);
    }

    private void onEdited(String p_213028_1_) {
        this.cleanUp();
    }

    private void updateLoginButton() {
        if (this.passwordWidget.getValue().isEmpty()) {
            this.mojangLoginButton.setMessage(new StringTextComponent("Login Offline"));
        } else {
            this.mojangLoginButton.setMessage(new StringTextComponent("Login"));
        }
    }

    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    public void onClose() {
        this.cleanUp();
        this.minecraft.setScreen(this.lastScreen);
    }

    private void cleanUp() {
        this.mojangLoginButton.active = !this.usernameWidget.getValue().isEmpty();
    }

    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        this.renderBackground(p_230430_1_);
        drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, 17, 16777215);
        drawString(p_230430_1_, this.font, "Username/Email", this.width / 2 - 100, this.height / 2 - 60 - 12, 10526880);
        drawString(p_230430_1_, this.font, "Password", this.width / 2 - 100, this.height / 2 - 20 - 12, 10526880);
        if (status.get() != null) {
            drawCenteredString(p_230430_1_, Minecraft.getInstance().font, status.get(), this.width / 2, this.height / 2 + 20, 0xFF0000);
        }
        this.usernameWidget.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        this.passwordWidget.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
//        if (this.savePasswordButton.isHovered()) {
//            List<ITextProperties> tooltips = new ArrayList<>();
//            String tooltip = "This will save your password encrypted to your config file. While the password is encrypted if a hacker accesses your computer they could easily unencrypt it.";
//            tooltips.add(ITextProperties.of(tooltip));
//            renderWrappedToolTip(p_230430_1_, tooltips, p_230430_2_, p_230430_3_, this.font);
//        }

        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
    }
}
