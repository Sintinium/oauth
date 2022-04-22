package com.sintinium.oauth.gui.profile;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class FakePlayer extends EntityOtherPlayerMP {
    private static FakePlayer instance;
    private ResourceLocation skin;
    private ResourceLocation cape = null;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public FakePlayer() {
        super(FakeWorld.getInstance(), Minecraft.getMinecraft().getSession().func_148256_e());
        
        Minecraft.getMinecraft().func_152342_ad().func_152790_a(getGameProfile(), (type, resourceLocation) -> {
            skin = resourceLocation;
        }, false);
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
                skin = locationStevePng;
                cape = null;
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
            Minecraft.getMinecraft().func_152342_ad().func_152790_a(profile, (type, resourceLocation) -> {
                if (type == MinecraftProfileTexture.Type.SKIN) {
                    this.skin = resourceLocation;
                    data.skin = skin;
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
    public ResourceLocation getLocationSkin() {
        if (skin == null) return locationStevePng;
        return skin;
    }
    
    @Nullable
    @Override
    public ResourceLocation getLocationCape() {
        return cape;
    }

    public boolean func_152122_n() {
        return cape != null;
    }
    
    @Override
    public double getDistanceSqToEntity(Entity e) {
    	if(e == null) return Double.MAX_VALUE;
    	return super.getDistanceSqToEntity(e);
    }
    
    @Override
    public float getDistanceToEntity(Entity e) {
    	if(e == null) return Float.MAX_VALUE;
    	return super.getDistanceToEntity(e);
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
