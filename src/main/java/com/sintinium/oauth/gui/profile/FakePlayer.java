package com.sintinium.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelPart;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FakePlayer extends LocalPlayer {

    private static FakePlayer instance;
    private ResourceLocation skin;
    private ResourceLocation cape = null;
    private String skinModel = "default";
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

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
        try {
            if (profile == null) {
                skin = DefaultPlayerSkin.getDefaultSkin();
                cape = null;
                skinModel = "default";
                return;
            }
//
//            if (cache.containsKey(profile.getId())) {
//                PlayerData data = cache.get(profile.getId());
//                this.skin = data.skin;
//                this.cape = data.cape;
//                this.skinModel = data.skinModel;
//                return;
//            }

            PlayerData data = new PlayerData();
            cape = null;
            Minecraft.getInstance().getSkinManager().registerSkins(profile, (type, resourceLocation, minecraftProfileTexture) -> {
                if (type == MinecraftProfileTexture.Type.SKIN) {
                    this.skin = resourceLocation;
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
            }, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public float distanceTo(Entity pEntity) {
        return Float.MAX_VALUE;
    }

    @Override
    public double distanceToSqr(Entity pEntity) {
        return Float.MAX_VALUE;
    }

}
