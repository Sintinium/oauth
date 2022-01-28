package com.sintinium.oauth;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("oauth")
public class OAuth {
    // Directly reference a log4j logger.
    private static OAuth INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger();
    public ModContainer modContainer;
    public static boolean debugMode = false;

    public OAuth() {
        INSTANCE = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);
        modContainer = ModList.get().getModContainerById("oauth").orElse(null);
    }

    public static OAuth getInstance() {
        return INSTANCE;
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // Set the mod to only run client side
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }
}
