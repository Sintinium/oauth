package com.sintinium.oauth;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiFunction;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("oauth")
public class OAuth {
    // Directly reference a log4j logger.
    private static OAuth INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger();
    public Config config;

    public OAuth() {
        INSTANCE = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);
        config = new Config();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, config.getSpec());
    }

    public static OAuth getInstance() {
        return INSTANCE;
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // Set the mod to only run client side
        Supplier<?> first = () -> FMLNetworkConstants.IGNORESERVERONLY;
        BiFunction<?, ?, ?> second = (a, b) -> true;
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true)
        );
    }

    @SubscribeEvent
    public void configSetup(ModConfigEvent event) {
        if (event.getConfig().getType() != ModConfig.Type.CLIENT) return;
        config.setup(event.getConfig());
    }

//    @SubscribeEvent
//    private void configSetup(ModConfig.ModConfigEvent event) {
//        if (event.getConfig().getType() != ModConfig.Type.CLIENT) return;
//
//        config.setup(event.getConfig());
//    }



}
