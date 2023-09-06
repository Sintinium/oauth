package me.jarva.oauth.profile;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.UserType;

import java.util.UUID;

public interface IProfile {
    String getName();

    void setName(String name);

    UUID getUUID();

    boolean login() throws Exception;

    JsonObject serialize();

    UserType getUserType();

    default GameProfile getGameProfile() {
        return ProfileManager.getInstance().getGameProfileOrNull(getUUID());
    }
}
