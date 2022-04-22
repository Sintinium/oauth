package com.sintinium.oauth.gui.profile;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.sintinium.oauth.gui.ActionButton;
import com.sintinium.oauth.gui.ErrorScreen;
import com.sintinium.oauth.gui.GuiScreenCustom;
import com.sintinium.oauth.gui.LoginLoadingScreen;
import com.sintinium.oauth.login.LoginUtil;
import com.sintinium.oauth.login.MicrosoftLogin;
import com.sintinium.oauth.profile.MicrosoftProfile;
import com.sintinium.oauth.profile.OfflineProfile;
import com.sintinium.oauth.profile.ProfileManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ReportedException;

public class ProfileSelectionScreen extends GuiScreenCustom {
    private ProfileList profileList;
    private GuiButton removeAccountButton;
    private GuiButton loginButton;
    private GuiButton loginOfflineButton;
    private ProfileEntry initialEntry;
    private GuiScreen lastScreen;

    public ProfileSelectionScreen(GuiScreen lastScreen) {
    	this.lastScreen = lastScreen;
    }

    public ProfileSelectionScreen(GuiScreen lastScreen, ProfileEntry initialEntry) {
        this(lastScreen);
        this.initialEntry = initialEntry;
    }

    public void onLoginButton() {
        onLoginButton(profileList.getSelected());
    }

    @Override
    public void setWorldAndResolution(Minecraft mcIn, int w, int h) {
        ProfileEntry selected = profileList != null ? profileList.getSelected() : null;
        super.setWorldAndResolution(mcIn, w, h);
        if (selected != null) {
            this.profileList.getEntryList().stream()
                    .filter(entry -> entry.getProfile().getUUID().equals(selected.getProfile().getUUID()))
                    .findFirst()
                    .ifPresent(profile -> this.profileList.setSelected(profile));
        }
    }

    @Override
	public void initGui() {
        profileList = new ProfileList(this, this.width, this.height, 32, this.height - 52, 16);
        profileList.loadProfiles();
        profileList.registerScrollButtons(7, 8);
        // clear the cache everytime this screen loads so new skins can load

        FakePlayer.getInstance().clearCache();
        if (LoginUtil.isOnline()) {
            // Duplicated because for some reason it only half loads the skin information. Running twice seems to fix it
            GameProfile profile = ProfileManager.getInstance().getGameProfileOrNull(Minecraft.getMinecraft().getSession().func_148256_e().getId());
            if (profile == null) profile = Minecraft.getMinecraft().getSession().func_148256_e();
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

        addButton(0, this.width / 2 - 45 - 90 - 2, this.height - 2 - 20, 90, "Add Account", () -> {
            onMicrosoftType(null);
        });
        removeAccountButton = addButton(1, this.width / 2 - 45, this.height - 2 - 20, 90, "Remove Account", () -> {
            if (profileList.getSelected() != null) {
                ProfileManager.getInstance().removeProfile(profileList.getSelected().getProfile().getUUID());
                int index = profileList.getIndex(profileList.getSelected());
                profileList.remove(profileList.getSelected());
                if (index < profileList.getSize() && index >= 0) {
                    profileList.setSelected(profileList.getListEntry(index));
                } else if (index <= profileList.getSize() && index > 0) {
                    profileList.setSelected(profileList.getListEntry(index - 1));
                } else {
                    profileList.setSelected(null);
                }
            }
        });
        removeAccountButton.enabled = false;
        addButton(2, this.width / 2 + 45 + 2, this.height - 2 - 20, 90, "Back", () -> setScreen(lastScreen));

        loginButton = addButton(3, this.width / 2 - 137, this.height - 4 - 40, 137, "Login", () -> onLoginButton());
        loginOfflineButton = addButton(4, this.width / 2 + 1, this.height - 4 - 40, 137, "Login Offline", () -> {
            if (profileList.getSelected() != null) {
                try {
                    LoginUtil.loginOffline(profileList.getSelected().getProfile().getName());
                    setScreen(lastScreen);
                } catch (LoginUtil.WrongMinecraftVersionException e) {
                    setScreen(new ErrorScreen(this, profileList.getSelected().getProfile() instanceof MicrosoftProfile, e));
                    e.printStackTrace();
                }
            }
        });
        loginButton.enabled = false;
        loginOfflineButton.enabled = false;

        if (this.initialEntry != null) {
            if (profileList.getIndex(this.initialEntry) >= 0) {
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
                setScreen(lastScreen);
                return;
            } catch (Exception e) {
                setScreen(new ErrorScreen(this, selected.getProfile() instanceof MicrosoftProfile, e));
                e.printStackTrace();
                return;
            }
        }

        // Async login.
        Thread thread = new Thread(() -> {
            try {
                final AtomicBoolean isCancelled = new AtomicBoolean();

                LoginLoadingScreen loginLoadingScreen = new LoginLoadingScreen(this, () -> {
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
                    onMicrosoftType(selected);
                    return;
                }
                if (!isSuccessful && Minecraft.getMinecraft().currentScreen instanceof ProfileSelectionScreen) {
                    setScreen(new ErrorScreen(Minecraft.getMinecraft().currentScreen, selected.getProfile() instanceof MicrosoftProfile, "Login Failed"));
                    return;
                }

                try {
                    GameProfile profile = LoginUtil.getGameProfile(Minecraft.getMinecraft().getSession());
                    if (profile != null) {
                        ProfileManager.getInstance().getProfile(selected.getProfile().getUUID()).setName(profile.getName());
                        ProfileManager.getInstance().save();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setScreen(lastScreen);
            } catch (UserMigratedException e) {
                ErrorScreen errorScreen = new ErrorScreen(this, selected.getProfile() instanceof MicrosoftProfile, "This account has migrated to Microsoft. Please create a new profile with your Microsoft account.");
                setScreen(errorScreen);
                e.printStackTrace();
            } catch (InvalidCredentialsException e) {
                // Exception was thrown and there's no message to display.
                if (e.getMessage() == null || e.getMessage().equals(e.getCause().toString())) {
                    ErrorScreen errorScreen = new ErrorScreen(this, selected.getProfile() instanceof MicrosoftProfile, e);
                    setScreen(errorScreen);
                } else {
                    ErrorScreen errorScreen = new ErrorScreen(this, profileList.getSelected().getProfile() instanceof MicrosoftProfile, e.getMessage() + ". Please delete the profile and create a new one.");
                    errorScreen.setInfo();
                    setScreen(errorScreen);
                }
                e.printStackTrace();
            } catch (MicrosoftLogin.BaseMicrosoftLoginException e) {
                setScreen(ErrorScreen.microsoftExceptionScreen(this, e));
                e.printStackTrace();
                return;
            } catch (Exception e) {
                setScreen(new ErrorScreen(this, profileList.getSelected().getProfile() instanceof MicrosoftProfile, e));
                e.printStackTrace();
            }
        }, "LoginThread");
        thread.setDaemon(true);
        thread.start();
    }

    private void onMicrosoftType(@Nullable ProfileEntry entry) {
        final MicrosoftLogin login = new MicrosoftLogin();
        LoginLoadingScreen loadingScreen = new LoginLoadingScreen(this, login::cancelLogin, true);
        login.setUpdateStatusConsumer(loadingScreen::updateText);
        Thread thread = new Thread(() -> {
            MicrosoftProfile profile;
            try {
                profile = login.login();
            } catch (MicrosoftLogin.BaseMicrosoftLoginException e) {
                setScreen(ErrorScreen.microsoftExceptionScreen(this, e));
                e.printStackTrace();
                LogManager.getLogger().error(login.getErroredResponses());
                return;
            } catch (Exception e) {
                setScreen(new ErrorScreen(this, true, e));
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

    private GuiButton addButton(int id, int x, int y, int width, String text, Runnable onPress) {
        return addButton(new ActionButton(id, x, y, width, 20, text, onPress));
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        this.profileList.func_148179_a(x, y, button);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.profileList.getSelected() == null) {
            removeAccountButton.enabled = false;
            loginButton.enabled = false;
            loginOfflineButton.enabled = false;
            return;
        }

        removeAccountButton.enabled = true;
        loginButton.enabled = !this.profileList.getSelected().isOffline();
        loginOfflineButton.enabled = true;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);
        int size = 60;
        int x = 40;
        int y = height / 2 + size;
        this.profileList.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glColor4f(1, 1, 1, 1);
        
        //load renderEngine and options just in case
        RenderManager.instance.renderEngine = mc.getTextureManager();
        RenderManager.instance.options = mc.gameSettings;
        
        //prevent crash with ArsMagica2
        EntityClientPlayerMP prevClientPlayer = mc.thePlayer;
        if(prevClientPlayer == null)
        	mc.thePlayer = new EntityClientPlayerMP(mc, FakeWorld.getInstance(), Minecraft.getMinecraft().getSession(), null, null);
        
        RenderManager.instance.getEntityRenderObject(FakePlayer.getInstance()).setRenderManager(RenderManager.instance);
        renderPlayer(x, y, size, x - mouseX, y - size * 2 + size / 2f - mouseY, FakePlayer.getInstance());
        
        //reset thePlayer
       	mc.thePlayer = prevClientPlayer;
       	
        mc.fontRenderer.drawStringWithShadow("Status: " + (LoginUtil.isOnline() ? "Online" : "Offline"), 12, 12, LoginUtil.isOnline() ? 0x55FF55 : 0xFF5555);
        drawCenteredString(mc.fontRenderer, "Current Profile: " + mc.getSession().getUsername(), width / 2, 12, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static void renderPlayer(int x, int y, int size, float lookX, float lookY, EntityLivingBase entity) {
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, 50.0F);
        GL11.glScalef((float)(-size), (float)size, (float)size);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f5 = entity.prevRotationYawHead;
        float f6 = entity.rotationYawHead;
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-((float)Math.atan((double)(lookY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = (float)Math.atan((double)(lookX / 40.0F)) * 20.0F;
        entity.rotationYaw = (float)Math.atan((double)(lookX / 40.0F)) * 40.0F;
        entity.rotationPitch = -((float)Math.atan((double)(lookY / 40.0F))) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
        GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180.0F;
        renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        entity.renderYawOffset = f2;
        entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.prevRotationYawHead = f5;
        entity.rotationYawHead = f6;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    //copied from RenderManager to avoid crash with ArchiSections
    public static void renderEntityWithPosYaw(Entity entity, double x, double y, double z, float rotation, float delta) {
        Render render = null;
        try {
            render = RenderManager.instance.getEntityRenderObject(entity);
            if (render != null && !render.isStaticEntity()) {
                try {
                    render.doRender(entity, x, y, z, rotation, delta);
                } catch (Throwable throwable) {
                    throw new ReportedException(CrashReport.makeCrashReport(throwable, "Rendering entity in world"));
                }
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being rendered");
            entity.addEntityCrashInfo(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
            crashreportcategory1.addCrashSection("Assigned renderer", render);
            crashreportcategory1.addCrashSection("Location", CrashReportCategory.func_85074_a(x, y, z));
            crashreportcategory1.addCrashSection("Rotation", Float.valueOf(rotation));
            crashreportcategory1.addCrashSection("Delta", Float.valueOf(delta));
            throw new ReportedException(crashreport);
        }
    }
}
