package com.sintinium.oauth;

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
}
