package me.jarva.oauth.forge;

#if POST_MC_1_16_5
import dev.architectury.platform.forge.EventBuses;
#else
import me.shedaniel.architectury.platform.forge.EventBuses;
#endif
import me.jarva.oauth.OAuth;
import me.jarva.oauth.OAuthClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

@Mod(OAuth.MOD_ID)
public class OAuthForge {
    public OAuthForge() {
        PreLaunchSetup.onPreLaunch();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::initClient);
        MinecraftForge.EVENT_BUS.register(this);

        OAuth.init();
    }

    private void initClient(final FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        OAuthClient.init();
    }
}
