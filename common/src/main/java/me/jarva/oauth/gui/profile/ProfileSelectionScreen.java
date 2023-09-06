package me.jarva.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jarva.oauth.gui.ErrorScreen;
import me.jarva.oauth.gui.LoginLoadingScreen;
import me.jarva.oauth.gui.MultiplayerDisabledScreen;
import me.jarva.oauth.gui.OAuthScreen;
import me.jarva.oauth.gui.components.OAuthButton;
import me.jarva.oauth.login.MicrosoftLogin;
import me.jarva.oauth.profile.MicrosoftProfile;
import me.jarva.oauth.profile.OfflineProfile;
import me.jarva.oauth.profile.ProfileManager;
import me.jarva.oauth.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

#if POST_CURRENT_MC_1_20_1
import net.minecraft.client.gui.GuiGraphics;
#else
import com.mojang.blaze3d.vertex.PoseStack;
#endif

public class ProfileSelectionScreen extends OAuthScreen {

    private ProfileList profileList;
    private Button removeAccountButton;
    private Button loginButton;
    private Button loginOfflineButton;
    private ProfileEntry initialEntry;

    public ProfileSelectionScreen() {
        super(ComponentUtils.literal("Profiles"));
    }

    public ProfileSelectionScreen(ProfileEntry initialEntry) {
        this();
        this.initialEntry = initialEntry;
    }

    public void onLoginButton() {
        onLoginButton(profileList.getSelected());
    }

    @Override
    public void resize(Minecraft p_96575_, int p_96576_, int p_96577_) {
        ProfileEntry selected = profileList.getSelected();
        super.resize(p_96575_, p_96576_, p_96577_);
        if (selected != null) {
            this.profileList.children().stream()
                    .filter(entry -> entry.getProfile().getUUID().equals(selected.getProfile().getUUID()))
                    .findFirst()
                    .ifPresent(profile -> this.profileList.setSelected(profile));
        }
    }

    @Override
    protected void init() {
        profileList = new ProfileList(this, Minecraft.getInstance(), this.width, this.height, 32, this.height - 52, 16);
        profileList.loadProfiles();
        // clear the cache everytime this screen loads so new skins can load

        FakePlayer.getInstance().clearCache();
        if (LoginUtil.isOnline()) {
            // Duplicated because for some reason it only half loads the skin information. Running twice seems to fix it
            GameProfile profile = ProfileManager.getInstance().getGameProfileOrNull(Minecraft.getInstance().getUser().getGameProfile().getId());
            if (profile == null) profile = Minecraft.getInstance().getUser().getGameProfile();
            FakePlayer.getInstance().setSkin(profile);
            FakePlayer.getInstance().setSkin(profile);
        } else if (profileList.getSelected() != null) {
            ProfileEntry entry = profileList.getSelected();
            // Duplicated because for some reason it only half loads the skin information. Running twice seems to fix it
            FakePlayer.getInstance().setSkin(entry.getProfile().getGameProfile());
            FakePlayer.getInstance().setSkin(entry.getProfile().getGameProfile());
        } else {
            FakePlayer.getInstance().setSkin(null);
        }

        addButton(this.width / 2 - 45 - 90 - 2, this.height - 2 - 20, 90, "Add Account", p_onPress_1_ -> {
            this.onMicrosoftType(null);
        });
        removeAccountButton = addButton(this.width / 2 - 45, this.height - 2 - 20, 90, "Remove Account", p_onPress_1_ -> {
            if (profileList.getSelected() != null) {
                ProfileManager.getInstance().removeProfile(profileList.getSelected().getProfile().getUUID());
                int index = profileList.children().indexOf(profileList.getSelected());
                profileList.children().remove(profileList.getSelected());
                if (profileList.children().isEmpty()) {
                    profileList.setSelected(null);
                } else if (index < profileList.children().size()) {
                    profileList.setSelected(profileList.children().get(index));
                } else if (index - 1 < profileList.children().size() && index - 1 >= 0) {
                    profileList.setSelected(profileList.children().get(index - 1));
                }
            }
        });
        removeAccountButton.active = false;
        addButton(this.width / 2 + 45 + 2, this.height - 2 - 20, 90, "Back", p_onPress_1_ -> setScreen(new TitleScreen()));

        loginButton = addButton(this.width / 2 - 137, this.height - 4 - 40, 137, "Login", p_onPress_1_ -> onLoginButton());
        loginOfflineButton = addButton(this.width / 2 + 1, this.height - 4 - 40, 137, "Login Offline", p_onPress_1_ -> {
            if (profileList.getSelected() != null) {
                try {
                    LoginUtil.loginOffline(profileList.getSelected().getProfile().getName());
                    setScreen(new JoinMultiplayerScreen(new TitleScreen()));
                } catch (LoginUtil.WrongMinecraftVersionException e) {
                    setScreen(new ErrorScreen(profileList.getSelected().getProfile() instanceof MicrosoftProfile, e));
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

    public void onLoginButton(ProfileEntry selected) {
        if (selected == null || selected.getProfile() == null) return;

        // Skip async if logging in offline.
        if (selected.getProfile() instanceof OfflineProfile) {
            try {
                selected.getProfile().login();
                Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
                return;
            } catch (Exception e) {
                setScreen(new ErrorScreen(selected.getProfile() instanceof MicrosoftProfile, e));
                e.printStackTrace();
                return;
            }
        }

        // Async login.
        Thread thread = new Thread(() -> {
            try {
                final AtomicBoolean isCancelled = new AtomicBoolean();

                LoginLoadingScreen loginLoadingScreen = new LoginLoadingScreen(() -> {
                    setScreen(new ProfileSelectionScreen(selected));
                    isCancelled.set(true);
                }, selected.getProfile() instanceof MicrosoftProfile);

                if (selected.getProfile() instanceof MicrosoftProfile) {
                    loginLoadingScreen.updateText("Logging into Microsoft.");
                } else {
                    loginLoadingScreen.updateText("Logging into Minecraft.");
                }
                setScreen(loginLoadingScreen);
                boolean isSuccessful = selected.getProfile().login();

                if (isCancelled.get()) {
                    return;
                }

                if (!isSuccessful && selected.getProfile() instanceof MicrosoftProfile) {
                    if (LoginUtil.isMultiplayerDisabled()) {
                        setScreen(new MultiplayerDisabledScreen());
                        return;
                    }
                    onMicrosoftType(selected);
                    return;
                }
                if (!isSuccessful && Minecraft.getInstance().screen instanceof ProfileSelectionScreen) {
                    setScreen(new ErrorScreen(selected.getProfile() instanceof MicrosoftProfile, "Login Failed"));
                    return;
                }

                try {
                    ProfileManager.getInstance().getProfile(selected.getProfile().getUUID()).setName(Minecraft.getInstance().getUser().getName());
                    ProfileManager.getInstance().save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setScreen(new JoinMultiplayerScreen(new TitleScreen()));
            } catch (UserMigratedException e) {
                ErrorScreen errorScreen = new ErrorScreen(selected.getProfile() instanceof MicrosoftProfile, "This account has migrated to Microsoft. Please create a new profile with your Microsoft account.");
                setScreen(errorScreen);
                e.printStackTrace();
            } catch (InvalidCredentialsException e) {
                // Exception was thrown and there's no message to display.
                if (e.getMessage() == null || e.getMessage().equals(e.getCause().toString())) {
                    ErrorScreen errorScreen = new ErrorScreen(selected.getProfile() instanceof MicrosoftProfile, e);
                    setScreen(errorScreen);
                } else {
                    ErrorScreen errorScreen = new ErrorScreen(profileList.getSelected().getProfile() instanceof MicrosoftProfile, e.getMessage() + ". Please delete the profile and create a new one.");
                    errorScreen.setInfo();
                    setScreen(errorScreen);
                }
                e.printStackTrace();
            } catch (MicrosoftLogin.BaseMicrosoftLoginException e) {
                setScreen(ErrorScreen.microsoftExceptionScreen(e));
                e.printStackTrace();
                return;
            } catch (Exception e) {
                setScreen(new ErrorScreen(profileList.getSelected().getProfile() instanceof MicrosoftProfile, e));
                e.printStackTrace();
            }
        }, "LoginThread");
        thread.setDaemon(true);
        thread.start();
    }

    private void onMicrosoftType(@Nullable ProfileEntry entry) {
        final MicrosoftLogin login = new MicrosoftLogin();
        LoginLoadingScreen loadingScreen = new LoginLoadingScreen(login::cancelLogin, true);
        login.setUpdateStatusConsumer(loadingScreen::updateText);
        Thread thread = new Thread(() -> {
            MicrosoftProfile profile;
            try {
                profile = login.login();
            } catch (MicrosoftLogin.BaseMicrosoftLoginException e) {
                setScreen(ErrorScreen.microsoftExceptionScreen(e));
                e.printStackTrace();
                LogManager.getLogger().error(login.getErroredResponses());
                return;
            } catch (Exception e) {
                setScreen(new ErrorScreen(true, e));
                e.printStackTrace();
                LogManager.getLogger().error(login.getErroredResponses());
                return;
            }
            if (profile != null) {
                ProfileManager.getInstance().addProfile(profile);
                ProfileEntry newProfile = new ProfileEntry(profileList, profile);
                onLoginButton(newProfile);
            }

        }, "Oauth microsoft");

        setScreen(loadingScreen);
        thread.setDaemon(true);
        thread.start();
    }

    private Button addButton(int x, int y, int width, String text, Button.OnPress onPress) {
        return this.addRenderableWidget(new OAuthButton(x, y, width, 20, ComponentUtils.literal(text), onPress));
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
    #if POST_CURRENT_MC_1_20_1
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    #else
    public void render(@NotNull PoseStack graphics, int mouseX, int mouseY, float delta) {
    #endif
        renderBackground(graphics);
        int size = 60;
        int x = 40;
        int y = height / 2 + size;
        this.profileList.render(graphics, mouseX, mouseY, delta);
        renderPlayer(RenderSystem.getModelViewStack(), mouseX, mouseY, delta);
        GuiUtils.drawShadow(graphics, ComponentUtils.literal("Status: " + (LoginUtil.isOnline() ? "Online" : "Offline")), 12, 12, LoginUtil.isOnline() ? 0x55FF55 : 0xFF5555);
        GuiUtils.drawCentered(graphics, ComponentUtils.literal("Current Profile: " + Minecraft.getInstance().getUser().getName()), width / 2, 12, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, delta);
    }

    private void renderPlayer(PoseStack stack, int mouseX, int mouseY, float delta) {
        int size = 60;
        int x = 40;
        int y = height / 2 + size;
        float rotX = -mouseX + x;
        float rotY = -mouseY + y - size * 2 + size / 2f;
        stack.pushPose();
        stack.translate(x, y, 1050.0);
        stack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();

        FakePlayer fakePlayer = FakePlayer.getInstance();
        float f = (float) Math.atan(rotX / 40.0F);
        float f1 = (float) Math.atan(rotY / 40.0F);

        fakePlayer.yBodyRot = 180.0F + f * 20.0F;
        fakePlayer.setYRot(180.0F + f * 40.0F);
        fakePlayer.setXRot(-f1 * 20.0F);
        fakePlayer.yHeadRot = fakePlayer.getYRot();
        fakePlayer.yHeadRotO = fakePlayer.getYRot();

        PoseStack playerStack = new PoseStack();
        playerStack.translate(0.0, 0.0, 1000.0);
        playerStack.scale(size, size, size);
        QuaternionUtil.rotatePlayerStack(playerStack, rotX);
        playerStack.scale(0.9375F, 0.9375F, 0.9375F);

        Lighting.setupForEntityInInventory();
        MultiBufferSource.BufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            renderEntity(playerStack, multiBufferSource);
        });
        multiBufferSource.endBatch();

        stack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    private void renderEntity(PoseStack stack, MultiBufferSource multiBufferSource) {
        renderModel(stack, multiBufferSource);
    }

    private void renderModel(PoseStack stack, MultiBufferSource multiBufferSource) {
        stack.pushPose();
        QuaternionUtil.rotatePlayerModel(stack);
        stack.scale(-1f, -1f, 1f);
        stack.translate(0.0, -1.501, 0.0);
        boolean slim = false;
        PlayerModel<AbstractClientPlayer> model;
        if (FakePlayer.getInstance().getModelName().equals("slim")) model = PlayerRenderers.slimPlayerModel;
        else model = PlayerRenderers.playerModel;
        model.prepareMobModel(FakePlayer.getInstance(), 0f, 0f, 1f);
        float rotation = Mth.rotLerp(1f, FakePlayer.getInstance().yHeadRotO, FakePlayer.getInstance().yHeadRot) - Mth.lerp(1f, FakePlayer.getInstance().yBodyRotO, FakePlayer.getInstance().yBodyRot);
        model.setupAnim(FakePlayer.getInstance(), 0f, 0f, 0f, rotation, Mth.lerp(1f, FakePlayer.getInstance().xRotO, FakePlayer.getInstance().getXRot()));

        RenderType renderType = model.renderType(FakePlayer.getInstance().getSkinTextureLocation());
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
        int overlayCoords = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(false));
        model.setAllVisible(true);
        model.young = false;

        model.renderToBuffer(stack, vertexConsumer, 15728880, overlayCoords, 1.0f, 1.0f, 1.0f, 1.0f);

        FakePlayer fakePlayer = FakePlayer.getInstance();
        if (fakePlayer.isCapeLoaded() && fakePlayer.isModelPartShown(PlayerModelPart.CAPE) && fakePlayer.getCloakTextureLocation() != null) {
            stack.pushPose();
            stack.translate(0.0D, 0.0D, 0.225D);
            stack.mulPose(AxisUtil.getZNRotation(5f));
            stack.mulPose(AxisUtil.getXPRotation(5f));
            VertexConsumer vertexConsumer1 = multiBufferSource.getBuffer(RenderType.entitySolid(fakePlayer.getCloakTextureLocation()));
            model.renderCloak(stack, vertexConsumer1, 15728880, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }

        stack.popPose();
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
    }
}
