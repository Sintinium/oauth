package com.sintinium.oauth.gui.profile;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import com.sintinium.oauth.profile.IProfile;
import com.sintinium.oauth.profile.OfflineProfile;
import com.sintinium.oauth.profile.ProfileManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

public class ProfileEntry implements GuiListExtended.IGuiListEntry {
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

    public void drawEntry(int index, int left, int top, int width, int height, Tessellator tessellator, int mouseX, int mouseY, boolean hovered) {
        String name = profile.getName();
        if (isOffline) name += " (Offline)";
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name, left, top + 2, 0xFFFFFF);
        if (!hovered && !Minecraft.getMinecraft().gameSettings.touchscreen) return;
    	Minecraft.getMinecraft().getTextureManager().bindTexture(ICON_OVERLAY_LOCATION);
        upArrow.setPosition(left + width, top);
        if (index > 0) {
            upArrow.render(mouseX, mouseY);
        }
        downArrow.setPosition(left + width, top);
        if (index < profileList.getSize() - 1) {
            downArrow.render(mouseX, mouseY);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control.
     */
    public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        if (index > 0 && upArrow.isMouseOver(x, y)) {
            movePosition(-1);
            return true;
        } else if (index < profileList.getSize() - 1 && downArrow.isMouseOver(x, y)) {
            movePosition(1);
            return true;
        }

        profileList.setSelected(this);

        if (System.currentTimeMillis() - this.lastClickTime < 250L) {
            profileList.getProfileSelectionScreen().onLoginButton(this);
        }

        this.lastClickTime = System.currentTimeMillis();
        return true;
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

    private class ArrowButton {
        private final int xOffset;
        private final int yOffset;
        private int x;
        private int y;
        private final int width = 11;
        private final int height = 7;
        private final float textureX;
        private final float textureY;

        public ArrowButton(int xOffset, int yOffset, float textureX, float textureY) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.textureX = textureX;
            this.textureY = textureY;
        }

        public void setPosition(int x, int y) {
            this.x = x + xOffset;
            this.y = y + yOffset;
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public void render(int mouseX, int mouseY) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(ICON_OVERLAY_LOCATION);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.func_146110_a(x, y, textureX, isMouseOver(mouseX, mouseY) ? textureY + 32 : textureY, width, height, 256, 256);
        }
    }
}
