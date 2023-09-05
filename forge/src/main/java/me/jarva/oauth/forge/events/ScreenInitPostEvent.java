package me.jarva.oauth.forge.events;

import me.jarva.oauth.OAuth;
import me.jarva.oauth.events.JoinMultiplayerScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OAuth.MOD_ID, value = Dist.CLIENT)
public class ScreenInitPostEvent {
    @SubscribeEvent
    #if POST_MC_1_18_2
    public static void postScreenOpen(ScreenEvent.Init.Post event) {
    #else
    public static void postScreenOpen(ScreenEvent.InitScreenEvent.Post event) {
    #endif
        if (event.getScreen() instanceof JoinMultiplayerScreen multiplayerScreen) {
            JoinMultiplayerScreenHandler.handle(Minecraft.getInstance(), multiplayerScreen);
        }
    }
}
