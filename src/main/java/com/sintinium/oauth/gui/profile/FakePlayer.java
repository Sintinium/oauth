package com.sintinium.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class FakePlayer extends ClientPlayerEntity {

    private static FakePlayer instance;
    private ResourceLocation skin;
    private ResourceLocation cape = null;
    private String skinModel = "default";

    public FakePlayer() {
        super(Minecraft.getInstance(), FakeWorld.getInstance(), FakeClientPlayNetHandler.getInstance(), null, null, false, false);
        Minecraft.getInstance().getSkinManager().registerSkins(getGameProfile(), (type, resourceLocation, minecraftProfileTexture) -> {
            skin = resourceLocation;
        }, true);
    }

    public static FakePlayer getInstance() {
        if (instance == null) {
            instance = new FakePlayer();
        }
        return instance;
    }

    public void setSkin(GameProfile profile) {
        cape = null;
        Minecraft.getInstance().getSkinManager().registerSkins(profile, (type, resourceLocation, minecraftProfileTexture) -> {
            if (type == MinecraftProfileTexture.Type.SKIN) {
                skin = resourceLocation;
                this.skinModel = minecraftProfileTexture.getMetadata("model");
                if (this.skinModel == null) {
                    this.skinModel = "default";
                }
            }
            if (type == MinecraftProfileTexture.Type.CAPE) {
                cape = resourceLocation;
            }
        }, true);
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart p_175148_1_) {
        return true;
    }

    @Override
    public ResourceLocation getSkinTextureLocation() {
        if (skin == null) return DefaultPlayerSkin.getDefaultSkin();
        return skin;
    }

    @Override
    public String getModelName() {
        return skinModel;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return true;
    }

    @Override
    public boolean isCapeLoaded() {
        return true;
    }

    @Nullable
    @Override
    public ResourceLocation getCloakTextureLocation() {
        return cape;
    }
}
