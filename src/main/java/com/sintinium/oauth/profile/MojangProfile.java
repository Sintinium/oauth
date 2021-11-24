package com.sintinium.oauth.profile;

import com.google.gson.JsonObject;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.sintinium.oauth.EncryptionUtil;
import com.sintinium.oauth.login.LoginUtil;

import java.util.UUID;

public class MojangProfile implements IProfile {

    private String name;
    private final String password;
    private final UUID uuid;
    private final UserType userType;

    public MojangProfile(String name, String password, UUID uuid, UserType userType) {
        this.name = name;
        this.password = password;
        this.uuid = uuid;
        this.userType = userType;
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
        if (!LoginUtil.loginMojangOrLegacy(this.name, this.password)) {
            return false;
        }
        LoginUtil.needsRefresh = true;
        return LoginUtil.isOnline();
    }

    public static MojangProfile deserialize(JsonObject json) throws Exception {
        String name = json.get("name").getAsString();
        String password = EncryptionUtil.decryptString(json.get("password").getAsString(), EncryptionUtil.key);
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        UserType userType = UserType.byName(json.get("userType").getAsString());

        return new MojangProfile(name, password, uuid, userType);
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", typeName());
        json.addProperty("name", this.name);
        json.addProperty("password", EncryptionUtil.encryptString(this.password, EncryptionUtil.key));
        json.addProperty("uuid", this.uuid.toString());
        json.addProperty("userType", this.userType.getName());
        return json;
    }

    public static String typeName() {
        return "mojang";
    }

    @Override
    public UserType getUserType() {
        return this.userType;
    }
}
