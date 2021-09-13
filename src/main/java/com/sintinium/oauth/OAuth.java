package com.sintinium.oauth;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(modid = "oauth", acceptableRemoteVersions = "*")
public class OAuth {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
            Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
            OAuthConfig.load(cfg);
        }
    }
}
