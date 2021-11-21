package com.sintinium.oauth.profile;

import com.mojang.authlib.UserType;
import org.json.JSONObject;

import java.util.UUID;

public interface IProfile {
    String getName();

    void setName(String name);

    UUID getUUID();

    /**
     * @return Returns true if login was successful.
     */
    boolean login() throws Exception;

    JSONObject serialize();

    UserType getUserType();
}
