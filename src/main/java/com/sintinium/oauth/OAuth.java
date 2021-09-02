package com.sintinium.oauth;

import com.sintinium.oauth.gui.LoginTypeScreen;
import com.sintinium.oauth.gui.TextWidget;
import com.sintinium.oauth.login.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
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
    public static boolean savePassword = false;
//    public final Config config;

    public OAuth() {
        INSTANCE = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);

//        if (FMLEnvironment.dist == Dist.CLIENT) {
//            config = new Config();
//            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, config.getSpec(), "oauth.toml");
//        } else {
//            config = null;
//        }
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

//    @SubscribeEvent
//    private void configSetup(ModConfig.ModConfigEvent event) {
//        if (event.getConfig().getType() != ModConfig.Type.CLIENT) return;
//
//        config.setup(event.getConfig());
//    }


    @SubscribeEvent
    public void multiplayerScreenOpen(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof JoinMultiplayerScreen)) return;
        JoinMultiplayerScreen multiplayerScreen = (JoinMultiplayerScreen) event.getGui();
        try {
//            Method addButtonMethod = ObfuscationReflectionHelper.findMethod(Screen.class, "addButton", Widget.class);
//            Method addButtonMethod = ObfuscationReflectionHelper.findMethod(Screen.class, "func_230480_a_", Widget.class);
            event.addWidget(new Button(10, 6, 66, 20, new TextComponent("Oauth Login"), p_onPress_1_ -> Minecraft.getInstance().setScreen(new LoginTypeScreen(multiplayerScreen))));
            final TextWidget textWidget = new TextWidget(10 + 66 + 3, 6, 0, 20, "");
            textWidget.setFGColor(0xFF5555);
            Thread thread = new Thread(() -> {
                boolean isOnline = LoginUtil.isOnline();
                if (isOnline) {
                    textWidget.setMessage(new TextComponent("Status: online"));
                    textWidget.setFGColor(0x55FF55);
                } else {
                    textWidget.setMessage(new TextComponent("Status: offline"));
                    textWidget.setFGColor(0xFF5555);
                }
            });
            thread.start();

            event.addWidget(textWidget);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
