package com.sintinium.oauth;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(modid = "oauth", clientSideOnly = true)
public class OAuth {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean savePassword = false;

    public OAuth() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
