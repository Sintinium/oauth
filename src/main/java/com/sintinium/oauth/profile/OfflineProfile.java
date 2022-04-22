package com.sintinium.oauth.profile;

import com.google.gson.JsonObject;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.sintinium.oauth.login.LoginUtil;

import java.util.UUID;

public class OfflineProfile implements IProfile {

    private String name;
    private final UUID uuid;

    public OfflineProfile(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public boolean login() throws AuthenticationException, LoginUtil.WrongMinecraftVersionException {
        LoginUtil.loginOffline(this.name);
        return true;
    }

    public static OfflineProfile deserialize(JsonObject json) {
        String name = json.get("name").getAsString();
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        return new OfflineProfile(name, uuid);
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", typeName());
        json.addProperty("name", this.name);
        json.addProperty("uuid", this.uuid.toString());
        return json;
    }

    public static String typeName() {
        return "offline";
    }

    @Override
    public UserType getUserType() {
        return UserType.LEGACY;
    }
}

