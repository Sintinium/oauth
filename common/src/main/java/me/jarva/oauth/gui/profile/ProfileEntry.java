package me.jarva.oauth.gui.profile;

import com.mojang.blaze3d.systems.RenderSystem;
#if POST_CURRENT_MC_1_20_1
import net.minecraft.client.gui.GuiGraphics;
#else
import com.mojang.blaze3d.vertex.PoseStack;
#endif
import me.jarva.oauth.profile.IProfile;
import me.jarva.oauth.profile.OfflineProfile;
import me.jarva.oauth.profile.ProfileManager;
import me.jarva.oauth.util.GuiUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ProfileEntry extends ObjectSelectionList.Entry<ProfileEntry> {

    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");

    private final IProfile profile;
    private final ProfileList profileList;
    private final boolean isOffline;
    private final ArrowButton upArrow;
    private final ArrowButton downArrow;
    private long lastClickTime = 0L;

    public ProfileEntry(ProfileList profileList, IProfile profile) {
        this.profileList = profileList;
        this.profile = profile;
        this.isOffline = profile instanceof OfflineProfile;
        this.upArrow = new ArrowButton(-18 - 13, 2, 114, 5);
        this.downArrow = new ArrowButton(-18, 2, 82, 20);
    }

    public IProfile getProfile() {
        return profile;
    }

    public boolean isOffline() {
        return isOffline;
    }

    @Override
    #if POST_CURRENT_MC_1_20_1
    public void render(GuiGraphics graphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
    #else
    public void render(PoseStack graphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
    #endif
        String name = profile.getName();
        if (isOffline) name += " (Offline)";
        GuiUtils.drawShadow(graphics, Component.literal(name), pLeft, pTop + 2, 0xFFFFFF);
        if (this.profileList.getSelected() == this) {
            upArrow.setSelected(true);
            downArrow.setSelected(true);
        } else {
            upArrow.setSelected(false);
            downArrow.setSelected(false);
        }
        if (!pIsMouseOver) return;
        upArrow.setPosition(pLeft + pWidth, pTop);
        downArrow.setPosition(pLeft + pWidth, pTop);
        if (profileList.children().indexOf(this) > 0) {
            upArrow.render(graphics, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTicks);
        }
        if (profileList.children().indexOf(this) < profileList.children().size() - 1) {
            downArrow.render(graphics, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTicks);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int i = profileList.children().indexOf(this);
        if (i > 0 && upArrow.isMouseOver((int) x, (int) y)) {
            movePosition(-1);
            return true;
        } else if (i < profileList.children().size() - 1 && downArrow.isMouseOver((int) x, (int) y)) {
            movePosition(1);
            return true;
        }

        profileList.setSelected(this);

        if (Util.getMillis() - this.lastClickTime < 250L) {
            profileList.getProfileSelectionScreen().onLoginButton(this);
        }

        this.lastClickTime = Util.getMillis();

        return true;
    }

    private void movePosition(int offset) {
        ProfileManager pm = ProfileManager.getInstance();
        try {
            int i = pm.getProfiles().indexOf(profile);
            pm.getProfiles().remove(i);
            pm.getProfiles().add(i + offset, profile);
            ProfileManager.getInstance().save();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            profileList.loadProfiles(profile.getUUID());
        }
    }

    public void onSelected() {
        if (isOffline) {
            FakePlayer.getInstance().setSkin(null);
            return;
        }
        FakePlayer.getInstance().setSkin(profile.getGameProfile());
    }

    @Override
    public @NotNull Component getNarration() {
        return Component.empty();
    }

    private static class ArrowButton {
        private final int xOffset;
        private final int yOffset;
        private int x;
        private int y;
        private final int width = 11;
        private final int height = 7;
        private boolean isSelected = false;
        private final float textureX;
        private final float textureY;

        public ArrowButton(int xOffset, int yOffset, float textureX, float textureY) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.textureX = textureX;
            this.textureY = textureY;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public void setPosition(int x, int y) {
            this.x = x + xOffset;
            this.y = y + yOffset;
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        #if POST_CURRENT_MC_1_20_1
        public void render(GuiGraphics graphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        #else
        public void render(PoseStack graphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        #endif
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, ICON_OVERLAY_LOCATION);
            RenderSystem.setShaderColor(1f, 1f, 1, 1f);
            if (isMouseOver(pMouseX, pMouseY)) {
                GuiUtils.blit(graphics, ICON_OVERLAY_LOCATION, x, y, textureX, textureY + 32, width, height, 256, 256);
            } else {
                GuiUtils.blit(graphics, ICON_OVERLAY_LOCATION, x, y, textureX, textureY, width, height, 256, 256);
            }
        }
    }
}
