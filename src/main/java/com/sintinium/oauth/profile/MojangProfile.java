package com.sintinium.oauth.profile;

import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.sintinium.oauth.EncryptionUtil;
import com.sintinium.oauth.login.LoginUtil;
import org.json.JSONObject;

import java.util.UUID;

public class MojangProfile implements IProfile {

    private String name;
    private final String password;
    private final UUID uuid;
    private UserType userType;

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

    @Override
    public JSONObject serialize() {
        JSONObject json = new JSONObject();
        json.put("type", typeName());
        json.put("name", this.name);
        json.put("password", EncryptionUtil.encryptString(this.password, EncryptionUtil.key));
        json.put("uuid", this.uuid.toString());
        json.put("userType", this.userType.getName());
        return json;
    }

    public static MojangProfile deserialize(JSONObject json) throws Exception {
        String name = json.getString("name");
        String password = EncryptionUtil.decryptString(json.getString("password"), EncryptionUtil.key);
        UUID uuid = UUID.fromString(json.getString("uuid"));
        UserType userType = UserType.byName(json.getString("userType"));

        return new MojangProfile(name, password, uuid, userType);
    }

    public static String typeName() {
        return "mojang";
    }

    @Override
    public UserType getUserType() {
        return this.userType;
    }
}
