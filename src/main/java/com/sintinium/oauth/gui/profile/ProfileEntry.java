package com.sintinium.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.sintinium.oauth.profile.IProfile;
import com.sintinium.oauth.profile.OfflineProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractList;

import java.util.UUID;

public class ProfileEntry extends AbstractList.AbstractListEntry<ProfileEntry> {

    private final IProfile profile;
    private final ProfileList profileList;
    private final boolean isOffline;

    public ProfileEntry(ProfileList profileList, IProfile profile) {
        this.profileList = profileList;
        this.profile = profile;
        this.isOffline = profile instanceof OfflineProfile;
    }

    public IProfile getProfile() {
        return profile;
    }

    public boolean isOffline() {
        return isOffline;
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        String name = profile.getName();
        if (isOffline) name += " (Offline)";
        Minecraft.getInstance().font.drawShadow(pMatrixStack, name, pLeft, pTop + 2, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        profileList.setSelected(this);
        onSelected();
        return true;
    }

    public void onSelected() {
        if (isOffline) {
            FakePlayer.getInstance().setSkin(null);
            return;
        }
        FakePlayer.getInstance().setSkin(new GameProfile(profile.getUUID(), profile.getName()));
    }
}
