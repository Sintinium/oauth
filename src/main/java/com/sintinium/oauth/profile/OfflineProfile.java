package com.sintinium.oauth.profile;

import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.sintinium.oauth.login.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import org.json.JSONObject;

import java.util.UUID;

public class OfflineProfile implements IProfile {

    private final String name;
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
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public boolean login() throws AuthenticationException, LoginUtil.WrongMinecraftVersionException {
        LoginUtil.loginOffline(this.name);
        return true;
    }

    @Override
    public JSONObject serialize() {
        JSONObject json = new JSONObject();
        json.put("type", typeName());
        json.put("name", this.name);
        json.put("uuid", this.uuid.toString());
        return json;
    }

    public static OfflineProfile deserialize(JSONObject json) {
        String name = json.getString("name");
        UUID uuid = UUID.fromString(json.getString("uuid"));
        return new OfflineProfile(name, uuid);
    }

    public static String typeName() {
        return "offline";
    }

    @Override
    public UserType getUserType() {
        return UserType.LEGACY;
    }
}

