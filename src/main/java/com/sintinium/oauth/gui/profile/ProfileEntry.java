package com.sintinium.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractList;

import java.util.UUID;

public class ProfileEntry extends AbstractList.AbstractListEntry<ProfileEntry> {

    private final String name;
    private final UUID uuid;
    private ProfileList profileList;

    public ProfileEntry(ProfileList profileList, UUID uuid, String name) {
        this.profileList = profileList;
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        Minecraft.getInstance().font.drawShadow(pMatrixStack, name, pLeft, pTop + 2, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        profileList.setSelected(this);
        FakePlayer.getInstance().setSkin(new GameProfile(uuid, name));
        return true;
    }
}
