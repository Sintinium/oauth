package com.sintinium.oauth.gui.profile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.sintinium.oauth.OAuth;
import com.sintinium.oauth.gui.*;
import com.sintinium.oauth.login.LoginUtil;
import com.sintinium.oauth.login.MicrosoftLogin;
import com.sintinium.oauth.profile.IProfile;
import com.sintinium.oauth.profile.MicrosoftProfile;
import com.sintinium.oauth.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class ProfileSelectionScreen extends OAuthScreen {

    private ProfileList profileList;
    private Button removeAccountButton;
    private Button loginButton;
    private Button loginOfflineButton;

    public ProfileSelectionScreen() {
        super(new StringTextComponent("Profiles"));
    }

    @Override
    protected void init() {
        profileList = new ProfileList(this, Minecraft.getInstance(), this.width, this.height, 32, this.height - 60, 16);
        // clear the cache everytime this screen loads so new skins can load
        FakePlayer.getInstance().clearCache();
        if (!LoginUtil.isOnline()) {
            FakePlayer.getInstance().setSkin(null);
        } else {
            FakePlayer.getInstance().setSkin(Minecraft.getInstance().getUser().getGameProfile());
        }

        for (IProfile profile : ProfileManager.getInstance().getProfiles()) {
            profileList.children().add(new ProfileEntry(profileList, profile));
        }

        addButton(this.width / 2 - 45 - 90 - 2, this.height - 2 - 20, 90, "Add Account", p_onPress_1_ -> {
            OAuth.getInstance().setScreen(new LoginTypeScreen(ProfileSelectionScreen::onMojangType, ProfileSelectionScreen::onMicrosoftType));
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

        loginButton = addButton(this.width / 2 - 137, this.height - 4 - 40, 137, "Login", p_onPress_1_ -> {
            if (profileList.getSelected() != null) {
                try {
                    boolean isSuccessful = profileList.getSelected().getProfile().login();
                    if (!isSuccessful && profileList.getSelected().getProfile() instanceof MicrosoftProfile) {
                        onMicrosoftType();
                        return;
                    }
                    if (!isSuccessful && Minecraft.getInstance().screen instanceof ProfileSelectionScreen) {
                        OAuth.getInstance().setScreen(new ErrorScreen(profileList.getSelected().getProfile() instanceof MicrosoftProfile, "Login Failed"));
                        return;
                    }
                    OAuth.getInstance().setScreen(new MultiplayerScreen(new MainMenuScreen()));
                } catch (Exception e) {
                    OAuth.getInstance().setScreen(new ErrorScreen(profileList.getSelected().getProfile() instanceof MicrosoftProfile, e));
                    e.printStackTrace();
                }
            }
        });
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
//        profileList.children().add(new ProfileEntry(profileList, UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), "Notch"));
//        profileList.children().add(new ProfileEntry(profileList, UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6"), "jeb_"));
    }

    private static void onMojangType() {
        OAuth.getInstance().setScreen(new LoginScreen());
    }

    private static void onMicrosoftType() {
        final MicrosoftLogin login = new MicrosoftLogin();
        LoginLoadingScreen loadingScreen = new LoginLoadingScreen(login::cancelLogin, true);
        login.setUpdateStatusConsumer(loadingScreen::updateText);
        Thread thread = new Thread(() -> {
            MicrosoftProfile profile = login.login();
            ProfileManager.getInstance().addProfile(profile);
            OAuth.getInstance().setScreen(new ProfileSelectionScreen());
        }, "Oauth microsoft");
        if (login.getErrorMsg() != null) {
            System.err.println(login.getErrorMsg());
        }
        OAuth.getInstance().setScreen(loadingScreen);
        thread.setDaemon(true);
        thread.start();
    }

    private Button addButton(int x, int y, int width, String text, Button.IPressable onPress) {
        return this.addButton(new Button(x, y, width, 20, new StringTextComponent(text), onPress));
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        // TODO remove this
        if (p_231044_5_ == 2) {
            OAuth.getInstance().setScreen(new ProfileSelectionScreen());
            return true;
        }
        return this.profileList.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_) | super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
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
        drawCenteredString(stack, font,"Current Profile: " + Minecraft.getInstance().getUser().getName(), width / 2, 12, 0xFFFFFF);

        super.render(stack, mouseX, mouseY, delta);
    }
}
