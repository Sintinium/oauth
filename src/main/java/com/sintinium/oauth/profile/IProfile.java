package com.sintinium.oauth.profile;

import com.google.gson.JsonObject;
import com.mojang.authlib.UserType;

import java.util.UUID;

public interface IProfile {
    String getName();

    void setName(String name);

    UUID getUUID();

    /**
     * @return Returns true if login was successful.
     */
    boolean login() throws Exception;

    JsonObject serialize();

    UserType getUserType();
}
