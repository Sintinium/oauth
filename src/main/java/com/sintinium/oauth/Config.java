package com.sintinium.oauth;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

public class Config {

    private final ForgeConfigSpec spec;
    private final ForgeConfigSpec.ConfigValue<String> savedUsername;
    private final ForgeConfigSpec.ConfigValue<String> savedPassword;

    public Config() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Saved username (if any)");
        savedUsername = builder.define("saved-username", "");
        builder.comment("Saved passwrd (if any)");
        savedPassword = builder.define("saved-password", "");
        spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }

    public void setup(ModConfig config) {
        if (config.getType() != ModConfig.Type.CLIENT) return;
        spec.acceptConfig(config.getConfigData());
    }

    public String getUsername() {
        return savedUsername.get();
    }

    public void setUsername(String username) {
        savedUsername.set(username);
    }

    public String getPassword() {
        return EncryptionUtil.decryptString(savedPassword.get(), FMLPaths.FMLCONFIG.get().toAbsolutePath().toString());
    }

    public void setPassword(String password) {
        savedPassword.set(EncryptionUtil.encryptString(password, FMLPaths.FMLCONFIG.get().toAbsolutePath().toString()));
    }

    public void removeUsernamePassword() {
        savedUsername.set("");
        savedPassword.set("");
    }

    public boolean isSavedPassword() {
        return !savedUsername.get().isEmpty() && !savedPassword.get().isEmpty();
    }
}
