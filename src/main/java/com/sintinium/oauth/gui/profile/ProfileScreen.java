package com.sintinium.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.UUID;

public class ProfileScreen extends Screen {
    public ProfileScreen() {
        super(new StringTextComponent("Profiles"));
    }

    @Override
    protected void init() {

    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        int size = 60;
        int x = 40;
        int y = height / 2 + size;
        InventoryScreen.renderEntityInInventory(x, y, size,-mouseX + x, -mouseY + y - size * 2 + size / 2f, FakePlayer.getInstance());
    }
}
