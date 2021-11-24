package com.sintinium.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.sintinium.oauth.OAuth;
import com.sintinium.oauth.gui.*;
import com.sintinium.oauth.login.LoginUtil;
import com.sintinium.oauth.login.MicrosoftLogin;
import com.sintinium.oauth.profile.MicrosoftProfile;
import com.sintinium.oauth.profile.OfflineProfile;
import com.sintinium.oauth.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProfileSelectionScreen extends OAuthScreen {

    private ProfileList profileList;
    private Button removeAccountButton;
    private Button loginButton;
    private Button loginOfflineButton;
    private ProfileEntry initialEntry;

    public ProfileSelectionScreen() {
        super(new StringTextComponent("Profiles"));
    }

    public ProfileSelectionScreen(ProfileEntry initialEntry) {
        this();
        this.initialEntry = initialEntry;
    }

    @Override
    protected void init() {
        profileList = new ProfileList(this, Minecraft.getInstance(), this.width, this.height, 32, this.height - 52, 16);
        profileList.loadProfiles();
        // clear the cache everytime this screen loads so new skins can load

        FakePlayer.getInstance().clearCache();
        if (LoginUtil.isOnline()) {
            // Duplicated because for some reason it only half loads the skin information. Running twice seems to fix it
            FakePlayer.getInstance().setSkin(Minecraft.getInstance().getUser().getGameProfile());
            FakePlayer.getInstance().setSkin(Minecraft.getInstance().getUser().getGameProfile());
        } else if (profileList.getSelected() != null) {
            ProfileEntry entry = profileList.getSelected();
            // Duplicated because for some reason it only half loads the skin information. Running twice seems to fix it
            FakePlayer.getInstance().setSkin(new GameProfile(entry.getProfile().getUUID(), entry.getProfile().getName()));
            FakePlayer.getInstance().setSkin(new GameProfile(entry.getProfile().getUUID(), entry.getProfile().getName()));
        } else {
            FakePlayer.getInstance().setSkin(null);
        }

        addButton(this.width / 2 - 45 - 90 - 2, this.height - 2 - 20, 90, "Add Account", p_onPress_1_ -> {
            OAuth.getInstance().setScreen(new LoginTypeScreen(ProfileSelectionScreen::onMojangType, () -> this.onMicrosoftType(null)));
        });
        removeAccountButton = addButton(this.width / 2 - 45, this.height - 2 - 20, 90, "Remove Account", p_onPress_1_ -> {
            if (profileList.getSelected() != null) {
                ProfileManager.getInstance().removeProfile(profileList.getSelected().getProfile().getUUID());
                int index = profileList.children().indexOf(profileList.getSelected());
                profileList.children().remove(profileList.getSelected());
                if (index < profileList.children().size()) {
                    profileList.setSelected(profileList.children().get(index));
                } else if (index - 1 < profileList.children().size() && index - 1 >= 0) {
                    profileList.setSelected(profileList.children().get(index - 1));
                }
            }
        });
        removeAccountButton.active = false;
        addButton(this.width / 2 + 45 + 2, this.height - 2 - 20, 90, "Back", p_onPress_1_ -> OAuth.getInstance().setScreen(new MainMenuScreen()));

        loginButton = addButton(this.width / 2 - 137, this.height - 4 - 40, 137, "Login", p_onPress_1_ -> onLoginButton());
        loginOfflineButton = addButton(this.width / 2 + 1, this.height - 4 - 40, 137, "Login Offline", p_onPress_1_ -> {
            if (profileList.getSelected() != null) {
                try {
                    LoginUtil.loginOffline(profileList.getSelected().getProfile().getName());
                    OAuth.getInstance().setScreen(new MultiplayerScreen(new MainMenuScreen()));
                } catch (LoginUtil.WrongMinecraftVersionException e) {
                    OAuth.getInstance().setScreen(new ErrorScreen(profileList.getSelected().getProfile() instanceof MicrosoftProfile, e));
                    e.printStackTrace();
                }
            }
        });
        loginButton.active = false;
        loginOfflineButton.active = false;

        this.addWidget(profileList);

        if (this.initialEntry != null) {
            if (profileList.children().contains(this.initialEntry)) {
                profileList.setSelected(this.initialEntry);
            }
            this.initialEntry = null;
        }
    }

    public void onLoginButton() {
        onLoginButton(profileList.getSelected());
    }

    public void onLoginButton(ProfileEntry selected) {
        if (selected == null) return;

        // Skip async if logging in offline.
        if (selected.getProfile() instanceof OfflineProfile) {
            try {
                selected.getProfile().login();
                Minecraft.getInstance().setScreen(new MultiplayerScreen(new MainMenuScreen()));
                return;
            } catch (Exception e) {
                OAuth.getInstance().setScreen(new ErrorScreen(profileList.getSelected().getProfile() instanceof MicrosoftProfile, e));
                e.printStackTrace();
                return;
            }
        }

        // Async login.
        Thread thread = new Thread(() -> {
            try {
                final AtomicBoolean isCancelled = new AtomicBoolean();

                LoginLoadingScreen loginLoadingScreen = new LoginLoadingScreen(() -> {
                    OAuth.getInstance().setScreen(new ProfileSelectionScreen(selected));
                    isCancelled.set(true);
                }, selected.getProfile() instanceof MicrosoftProfile);

                if (selected.getProfile() instanceof MicrosoftProfile) {
                    loginLoadingScreen.updateText("Logging into Microsoft.");
                } else {
                    loginLoadingScreen.updateText("Logging into Minecraft.");
                }
                OAuth.getInstance().setScreen(loginLoadingScreen);
                boolean isSuccessful = selected.getProfile().login();

                if (isCancelled.get()) {
                    return;
                }

                if (!isSuccessful && selected.getProfile() instanceof MicrosoftProfile) {
                    onMicrosoftType(selected);
                    return;
                }
                if (!isSuccessful && Minecraft.getInstance().screen instanceof ProfileSelectionScreen) {
                    OAuth.getInstance().setScreen(new ErrorScreen(selected.getProfile() instanceof MicrosoftProfile, "Login Failed"));
                    return;
                }

                try {
                    GameProfile profile = LoginUtil.getGameProfile(Minecraft.getInstance().getUser());
                    if (profile != null) {
                        ProfileManager.getInstance().getProfile(selected.getProfile().getUUID()).setName(profile.getName());
                        ProfileManager.getInstance().save();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                OAuth.getInstance().setScreen(new MultiplayerScreen(new MainMenuScreen()));
            } catch (InvalidCredentialsException e) {
                ErrorScreen errorScreen = new ErrorScreen(profileList.getSelected().getProfile() instanceof MicrosoftProfile, "Wrong Password. Please delete the profile and create a new one.");
                errorScreen.setInfo();
                OAuth.getInstance().setScreen(errorScreen);
            } catch (Exception e) {
                OAuth.getInstance().setScreen(new ErrorScreen(profileList.getSelected().getProfile() instanceof MicrosoftProfile, e));
                e.printStackTrace();
            }
        }, "LoginThread");
        thread.setDaemon(true);
        thread.start();
    }

    private static void onMojangType() {
        OAuth.getInstance().setScreen(new LoginScreen());
    }

    private void onMicrosoftType(@Nullable ProfileEntry entry) {
        final MicrosoftLogin login = new MicrosoftLogin();
        LoginLoadingScreen loadingScreen = new LoginLoadingScreen(login::cancelLogin, true);
        login.setUpdateStatusConsumer(loadingScreen::updateText);
        Thread thread = new Thread(() -> {
            MicrosoftProfile profile;
            try {
                profile = login.login();
            } catch (Exception e) {
                OAuth.getInstance().setScreen(new ErrorScreen(true, e));
                e.printStackTrace();
                return;
            }
            if (profile != null) {
                ProfileManager.getInstance().addProfile(profile);
            }

            ProfileEntry newProfile = new ProfileEntry(profileList, profile);
            onLoginButton(newProfile);
        }, "Oauth microsoft");

        OAuth.getInstance().setScreen(loadingScreen);
        thread.setDaemon(true);
        thread.start();
    }

    private Button addButton(int x, int y, int width, String text, Button.IPressable onPress) {
        return this.addButton(new Button(x, y, width, 20, new StringTextComponent(text), onPress));
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
/*
        Used for rapid debugging. Basically reloads the screen when moving buttons and such around.
        if (p_231044_5_ == 2) {
            OAuth.getInstance().setScreen(new ProfileSelectionScreen());
            return true;
        }
*/
        return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.profileList.getSelected() == null) {
            removeAccountButton.active = false;
            loginButton.active = false;
            loginOfflineButton.active = false;
            return;
        }

        removeAccountButton.active = true;
        loginButton.active = true;
        loginOfflineButton.active = true;

        if (this.profileList.getSelected().isOffline()) {
            loginButton.active = false;
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack);
        int size = 60;
        int x = 40;
        int y = height / 2 + size;
        this.profileList.render(stack, mouseX, mouseY, delta);
        InventoryScreen.renderEntityInInventory(x, y, size, -mouseX + x, -mouseY + y - size * 2 + size / 2f, FakePlayer.getInstance());
        Minecraft.getInstance().font.drawShadow(stack, "Status: " + (LoginUtil.isOnline() ? "Online" : "Offline"), 12, 12, LoginUtil.isOnline() ? 0x55FF55 : 0xFF5555);
        drawCenteredString(stack, font, "Current Profile: " + Minecraft.getInstance().getUser().getName(), width / 2, 12, 0xFFFFFF);

        super.render(stack, mouseX, mouseY, delta);
    }
}
