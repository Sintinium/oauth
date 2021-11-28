package com.sintinium.oauth;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Config;

@Config(modid = "oauth")
public class OAuthConfig {
    @Config.Comment({
            "THIS SHOULDN'T BE EDITED BY HAND!",
            "Last username used to login to Mojang if saved."
    })
    @Config.Name("Last Username")
    public static String lastUsername = "";

    @Config.Comment({
            "THIS SHOULDN'T BE EDITED BY HAND!",
            "Last password used to login to Mojang if saved. (Encrypted)"
    })
    @Config.Name("Last Password")
    public static String lastPassword = "";

    public static String getUsername() {
        return lastUsername;
    }

    public static void setUsername(String username) {
        lastUsername = username;
    }

    public static String getPassword() {
        return EncryptionUtil.decryptString(lastPassword, Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replaceAll("\\\\", "/"));
    }

    public static void setPassword(String password) {
        lastPassword = EncryptionUtil.encryptString(password, Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replaceAll("\\\\", "/"));
    }

    public static void removeUsernamePassword() {
        lastUsername = "";
        lastPassword = "";
    }

    public static boolean isSavedPassword() {
        return !lastUsername.isEmpty() && !lastPassword.isEmpty();
    }
}
