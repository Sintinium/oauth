package com.sintinium.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.UUID;

public class ProfileSelectionScreen extends Screen {

    private ProfileList profileList;

    public ProfileSelectionScreen() {
        super(new StringTextComponent("Profiles"));
    }

    @Override
    protected void init() {
        // clear the cache everytime this screen loads so new skins can load
        FakePlayer.getInstance().clearCache();

        profileList = new ProfileList(this, this.minecraft, this.width, this.height, 32, this.height - 32, 16);
        this.addWidget(profileList);
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        return this.profileList.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        int size = 60;
        int x = 40;
        int y = height / 2 + size;
        this.profileList.render(stack, mouseX, mouseY, delta);
        InventoryScreen.renderEntityInInventory(x, y, size,-mouseX + x, -mouseY + y - size * 2 + size / 2f, FakePlayer.getInstance());
    }
}
