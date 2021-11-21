package com.sintinium.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakePlayer extends ClientPlayerEntity {

    private static FakePlayer instance;
    private ResourceLocation skin;
    private ResourceLocation cape = null;
    private String skinModel = "default";
    private Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

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
        if (profile == null) {
            skin = DefaultPlayerSkin.getDefaultSkin();
            cape = null;
            skinModel = "default";
            return;
        }
        if (cache.containsKey(profile.getId())) {
            PlayerData data = cache.get(profile.getId());
            this.skin = data.skin;
            this.cape = data.cape;
            this.skinModel = data.skinModel;
            return;
        }

        PlayerData data = new PlayerData();
        cape = null;
        Minecraft.getInstance().getSkinManager().registerSkins(profile, (type, resourceLocation, minecraftProfileTexture) -> {
            if (type == MinecraftProfileTexture.Type.SKIN) {
                skin = resourceLocation;
                this.skinModel = minecraftProfileTexture.getMetadata("model");
                if (this.skinModel == null) {
                    this.skinModel = "default";
                }
                data.skin = skin;
                data.skinModel = skinModel;
                cache.put(profile.getId(), data);
            }
            if (type == MinecraftProfileTexture.Type.CAPE) {
                cape = resourceLocation;
                data.cape = cape;
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

    public void clearCache() {
        // clear the cache so skins are updated
        cache.clear();
    }

    private static class PlayerData {
        private ResourceLocation skin;
        private ResourceLocation cape;
        private String skinModel;
    }
}
