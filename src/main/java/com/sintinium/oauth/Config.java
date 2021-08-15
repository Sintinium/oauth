package com.sintinium.oauth;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class Config {


    private final ForgeConfigSpec spec;
    private final ForgeConfigSpec.ConfigValue<String> savedUsername;
    private final ForgeConfigSpec.ConfigValue<String> savedPassword;
    private boolean hasSavedPassword = false;

    public Config() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        savedUsername = builder.define("saved-username", "");
        savedPassword = builder.define("saved-password", "");
        spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }


    public void setup(ModConfig config) {
        if (config.getType() != ModConfig.Type.CLIENT) return;
    }

    public String getUsername() {
        return savedUsername.get();
    }

    public void setUsername(String username) {
        savedUsername.set(username);
    }

    public String getPassword() {
        return savedPassword.get();
    }

    public void setPassword(String password) {
        savedPassword.set(password);
    }

    public boolean isSavedPassword() {
        return hasSavedPassword;
    }
}
