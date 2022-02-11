package com.sintinium.oauth.gui;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.sintinium.oauth.gui.profile.ProfileSelectionScreen;
import com.sintinium.oauth.login.LoginUtil;
import com.sintinium.oauth.profile.MojangProfile;
import com.sintinium.oauth.profile.OfflineProfile;
import com.sintinium.oauth.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class LoginScreen extends OAuthScreen {

    private Button mojangLoginButton;
    private PasswordFieldWidget passwordWidget;
    private TextFieldWidget usernameWidget;
    private final AtomicReference<String> status = new AtomicReference<>();

    private final List<Runnable> toRun = new CopyOnWriteArrayList<>();

    public LoginScreen() {
        super(new StringTextComponent("OAuth Login"));
    }

    public void tick() {
        super.tick();
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
        this.setInitialFocus(this.usernameWidget);
        this.usernameWidget.setResponder(this::onEdited);

        this.children.add(this.usernameWidget);
        this.children.add(this.passwordWidget);

        this.mojangLoginButton = this.addButton(new ResponsiveButton(this.width / 2 - 100, this.height / 2 + 36, 200, 20, new StringTextComponent("Add Profile"), (p_213030_1_) -> {
            Thread thread = new Thread(() -> {
                if (usernameWidget.getValue().isEmpty()) {
                    toRun.add(() -> this.status.set("Missing username!"));
                } else {
                    if (passwordWidget.getValue().isEmpty()) {
                        ProfileManager.getInstance().addProfile(new OfflineProfile(usernameWidget.getValue(), UUID.nameUUIDFromBytes(usernameWidget.getValue().getBytes())));
                        toRun.add(() -> setScreen(new ProfileSelectionScreen()));
                        return;
                    }
                    MojangProfile profile;
                    try {
                        profile = LoginUtil.tryGetMojangProfile(usernameWidget.getValue(), passwordWidget.getValue());
                    } catch (InvalidCredentialsException e) {
                        toRun.add(() -> this.status.set("Invalid username or password!"));
                        return;
                    } catch (AuthenticationUnavailableException e) {
                        toRun.add(() -> this.status.set("You seem to be offline. Check your connection!"));
                        e.printStackTrace();
                        return;
                    } catch (AuthenticationException e) {
                        toRun.add(() -> setScreen(new ErrorScreen(false, e)));
                        e.printStackTrace();
                        return;
                    }
                    if (profile == null) {
                        toRun.add(() -> this.status.set("Invalid username or password!"));
                    } else {
                        LoginUtil.updateOnlineStatus();
                        ProfileManager.getInstance().addProfile(profile);
                        toRun.add(() -> setScreen(new ProfileSelectionScreen()));
                    }
                }
            }, "Oauth mojang");
            thread.setDaemon(true);
            thread.start();
        }, this::updateLoginButton, () -> this.mojangLoginButton.setMessage(new StringTextComponent("Add Profile"))));

        this.addButton(new Button(this.width / 2 - 100, this.height / 2 + 60, 200, 20, DialogTexts.GUI_CANCEL, (p_213029_1_) -> {
            setScreen(new ProfileSelectionScreen());
        }));

        this.cleanUp();
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
            this.mojangLoginButton.setMessage(new StringTextComponent("Add Offline Profile"));
        } else {
            this.mojangLoginButton.setMessage(new StringTextComponent("Add Profile"));
        }
    }

    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    public void onClose() {
        this.cleanUp();
        this.minecraft.setScreen(new ProfileSelectionScreen());
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
