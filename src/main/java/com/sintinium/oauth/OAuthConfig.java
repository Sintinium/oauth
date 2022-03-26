package com.sintinium.oauth;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class OAuthConfig {
    private static Configuration cfg;
    private static Property lastUsername;
    private static Property lastPassword;

    public static void load(Configuration cfg) {
        OAuthConfig.cfg = cfg;
        OAuthConfig.lastUsername = cfg.get("oauth", "Last Username", "", "THIS SHOULDN'T BE EDITED BY HAND!\nLast username used to login to Mojang if saved.");
        OAuthConfig.lastPassword = cfg.get("oauth", "Last Password", "", "THIS SHOULDN'T BE EDITED BY HAND!\nLast password used to login to Mojang if saved. (Encrypted)");
        cfg.save();
    }

    public static String getUsername() {
        return lastUsername.getString();
    }

    public static void setUsername(String username) {
        lastUsername.set(username);
        cfg.save();
    }

    public static String getPassword() {
        return EncryptionUtil.decryptString(lastPassword.getString(), Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replaceAll("\\\\", "/"));
    }

    public static void setPassword(String password) {
        lastPassword.set(EncryptionUtil.encryptString(password, Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replaceAll("\\\\", "/")));
        cfg.save();
    }

    public static void removeUsernamePassword() {
        lastUsername.set("");
        lastPassword.set("");
        cfg.save();
    }

    public static boolean isSavedPassword() {
        return !lastUsername.getString().isEmpty() && !lastPassword.getString().isEmpty();
    }
}
