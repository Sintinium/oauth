package com.sintinium.oauth.profile;

import com.mojang.authlib.UserType;
import com.sintinium.oauth.login.LoginUtil;
import com.sintinium.oauth.login.MicrosoftLogin;
import org.json.JSONObject;

import java.util.UUID;

public class MicrosoftProfile implements IProfile {

    private final String name;
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
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public boolean login() throws Exception {
        if (accessToken == null && refreshToken != null) {
            MicrosoftProfile profile = new MicrosoftLogin().loginFromRefresh(refreshToken);
            if (accessToken == null) {
                return false;
            }
            accessToken = profile.accessToken;
            refreshToken = profile.refreshToken;
            ProfileManager.getInstance().save();
        }
        LoginUtil.loginMs(this);
        LoginUtil.needsRefresh = true;
        return LoginUtil.isOnline();
    }

    @Override
    public JSONObject serialize() {
        JSONObject json = new JSONObject();
        json.put("type", typeName());
        json.put("name", name);
        json.put("uuid", uuid.toString());
        json.put("refreshToken", refreshToken);
        return json;
    }

    public static MicrosoftProfile deserialize(JSONObject json) {
        String name = json.getString("name");
        UUID uuid = UUID.fromString(json.getString("uuid"));
        String refreshToken = json.getString("refreshToken");
        return new MicrosoftProfile(name, uuid, null, refreshToken);
    }

    public static String typeName() {
        return "microsoft";
    }

    @Override
    public UserType getUserType() {
        return UserType.MOJANG;
    }
}
