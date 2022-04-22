package com.sintinium.oauth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(modid = "oauth", acceptableRemoteVersions = "*")
public class OAuth {
	@Instance
	public static OAuth INSTANCE;
    public ModContainer modContainer;

    public OAuth() {
        modContainer = Loader.instance().getIndexedModList().get("oauth");
    }
	
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
        }
    }
}
