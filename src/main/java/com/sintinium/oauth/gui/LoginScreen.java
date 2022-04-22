package com.sintinium.oauth.gui;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.input.Keyboard;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.sintinium.oauth.login.LoginUtil;
import com.sintinium.oauth.profile.MojangProfile;
import com.sintinium.oauth.profile.OfflineProfile;
import com.sintinium.oauth.profile.ProfileManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class LoginScreen extends GuiScreenCustom {
    private final GuiScreen lastScreen;
    private ActionButton mojangLoginButton;
    private PasswordFieldWidget passwordWidget;
    private GuiTextField usernameWidget;
    private AtomicReference<String> status = new AtomicReference<>();
    private static final String title = "OAuth Login";

    private List<Runnable> toRun = new CopyOnWriteArrayList<>();

    public LoginScreen(GuiScreen last) {
        this.lastScreen = last;
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
    	super.updateScreen();
        this.usernameWidget.updateCursorCounter();
        this.passwordWidget.updateCursorCounter();
        if (usernameWidget.isFocused()) passwordWidget.setFocused(false);
        if (passwordWidget.isFocused()) usernameWidget.setFocused(false);
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

        this.mojangLoginButton = this.addButton(new ResponsiveButton(2, this.width / 2 - 100, this.height / 2 + 36, 200, 20, "Add Profile", () -> {
            Thread thread = new Thread(() -> {
                if (usernameWidget.getText().isEmpty()) {
                    toRun.add(() -> this.status.set("Missing username!"));
                } else {
                    if (passwordWidget.getText().isEmpty()) {
                        ProfileManager.getInstance().addProfile(new OfflineProfile(usernameWidget.getText(), UUID.nameUUIDFromBytes(usernameWidget.getText().getBytes())));
                        toRun.add(() -> setScreen(lastScreen));
                        return;
                    }
                    MojangProfile profile;
                    try {
                        profile = LoginUtil.tryGetMojangProfile(usernameWidget.getText(), passwordWidget.getText());
                    } catch (InvalidCredentialsException e) {
                        toRun.add(() -> this.status.set("Invalid username or password!"));
                        return;
                    } catch (AuthenticationUnavailableException e) {
                        toRun.add(() -> this.status.set("You seem to be offline. Check your connection!"));
                        e.printStackTrace();
                        return;
                    } catch (AuthenticationException e) {
                        toRun.add(() -> setScreen(new ErrorScreen(lastScreen, false, e)));
                        e.printStackTrace();
                        return;
                    }
                    if (profile == null) {
                        toRun.add(() -> this.status.set("Invalid username or password!"));
                    } else {
                        LoginUtil.updateOnlineStatus();
                        ProfileManager.getInstance().addProfile(profile);
                        toRun.add(() -> setScreen(lastScreen));
                    }
                }
            }, "Oauth mojang");
            thread.setDaemon(true);
            thread.start();
        }, this::updateLoginButton, () -> this.mojangLoginButton.displayString = "Add Profile"));
        
        this.addButton(new ActionButton(3, this.width / 2 - 100, this.height / 2 + 60, 200, 20, "Cancel", () -> setScreen(lastScreen)));

        this.cleanUp();
    }

    private void updateLoginButton() {
        if (this.passwordWidget.getText().isEmpty()) {
            this.mojangLoginButton.displayString = "Add Offline Profile";
        } else {
            this.mojangLoginButton.displayString = "Add Profile";
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
