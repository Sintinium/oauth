package com.sintinium.oauth.gui.profile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sintinium.oauth.profile.IProfile;
import com.sintinium.oauth.profile.OfflineProfile;
import com.sintinium.oauth.profile.ProfileManager;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
    public void render(PoseStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        String name = profile.getName();
        if (isOffline) name += " (Offline)";
        Minecraft.getInstance().font.drawShadow(pMatrixStack, name, pLeft, pTop + 2, 0xFFFFFF);
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
            upArrow.render(pMatrixStack, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTicks);
        }
        if (profileList.children().indexOf(this) < profileList.children().size() - 1) {
            downArrow.render(pMatrixStack, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTicks);
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
        return new TextComponent("");
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

        public void render(PoseStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, ICON_OVERLAY_LOCATION);
            RenderSystem.setShaderColor(1f, 1f, 1, 1f);
            if (isMouseOver(pMouseX, pMouseY)) {
                GuiComponent.blit(pMatrixStack, x, y, textureX, textureY + 32, width, height, 256, 256);
            } else {
                GuiComponent.blit(pMatrixStack, x, y, textureX, textureY, width, height, 256, 256);
            }
        }
    }
}
