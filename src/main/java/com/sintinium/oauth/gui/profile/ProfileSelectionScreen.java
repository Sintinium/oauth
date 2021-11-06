package com.sintinium.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.UUID;

public class ProfileSelectionScreen extends Screen {

    private ProfileList profileList;
    private Button removeAccountButton;
    private Button loginButton;
    private Button loginOfflineButton;

    public ProfileSelectionScreen() {
        super(new StringTextComponent("Profiles"));
    }

    @Override
    protected void init() {
        // clear the cache everytime this screen loads so new skins can load
        FakePlayer.getInstance().clearCache();

        profileList = new ProfileList(this, this.minecraft, this.width, this.height, 32, this.height - 60, 16);

        addButton(this.width / 2 - 45 - 90 - 2, this.height - 2 - 20, 90, "Add Account", p_onPress_1_ -> Minecraft.getInstance().setScreen(new MainMenuScreen()));
        removeAccountButton = addButton(this.width / 2 - 45, this.height - 2 - 20, 90, "Remove Account", p_onPress_1_ -> {});
        removeAccountButton.active = false;
        addButton(this.width / 2 + 45 + 2, this.height - 2 - 20, 90, "Back", p_onPress_1_ -> Minecraft.getInstance().setScreen(new MainMenuScreen()));

        loginButton = addButton(this.width / 2 - 137, this.height - 4 - 40, 137, "Login", p_onPress_1_ -> Minecraft.getInstance().setScreen(new MainMenuScreen()));
        loginOfflineButton = addButton(this.width / 2 + 1, this.height - 4 - 40, 137, "Login Offline", p_onPress_1_ -> Minecraft.getInstance().setScreen(new MainMenuScreen()));
        loginButton.active = false;
        loginOfflineButton.active = false;

        this.addWidget(profileList);
    }

    private Button addButton(int x, int y, int width, String text, Button.IPressable onPress) {
        return this.addButton(new Button(x, y, width, 20, new StringTextComponent(text), onPress));
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        // TODO remove this
        if (p_231044_5_ == 2) {
            Minecraft.getInstance().setScreen(new ProfileSelectionScreen());
            return true;
        }
        return this.profileList.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_) | super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack);
        int size = 60;
        int x = 40;
        int y = height / 2 + size;
        this.profileList.render(stack, mouseX, mouseY, delta);
        InventoryScreen.renderEntityInInventory(x, y, size,-mouseX + x, -mouseY + y - size * 2 + size / 2f, FakePlayer.getInstance());

        super.render(stack, mouseX, mouseY, delta);
    }
}
