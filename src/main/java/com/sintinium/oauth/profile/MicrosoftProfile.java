package com.sintinium.oauth.profile;

import com.google.gson.JsonObject;
import com.mojang.authlib.UserType;
import com.sintinium.oauth.login.LoginUtil;
import com.sintinium.oauth.login.MicrosoftLogin;
import org.apache.logging.log4j.LogManager;

import java.util.UUID;

public class MicrosoftProfile implements IProfile {

    private String name;
    private final UUID uuid;
    private String accessToken;
    private String refreshToken;

    public MicrosoftProfile(String name, UUID uuid, String accessToken, String refreshToken) {
        this.name = name;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
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
    public boolean login() throws Exception {
        MicrosoftLogin login = new MicrosoftLogin();
        try {
            if (accessToken == null && refreshToken != null) {
                MicrosoftProfile profile = login.loginFromRefresh(refreshToken);
                if (profile == null) {
                    return false;
                }
                accessToken = profile.accessToken;
                refreshToken = profile.refreshToken;
                if (accessToken == null) {
                    return false;
                }
                ProfileManager.getInstance().save();
            }
            LoginUtil.loginMs(this);
            LoginUtil.setOnline(true);
            return true;
        } catch (Exception e) {
            LogManager.getLogger().error(login.getErroredResponses());
            throw e;
        }
    }

    public static MicrosoftProfile deserialize(JsonObject json) {
        String name = json.get("name").getAsString();
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        String refreshToken = json.get("refreshToken").getAsString();
        return new MicrosoftProfile(name, uuid, null, refreshToken);
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", typeName());
        json.addProperty("name", name);
        json.addProperty("uuid", uuid.toString());
        json.addProperty("refreshToken", refreshToken);
        return json;
    }

    public static String typeName() {
        return "microsoft";
    }

    @Override
    public UserType getUserType() {
        return UserType.MOJANG;
    }
}
