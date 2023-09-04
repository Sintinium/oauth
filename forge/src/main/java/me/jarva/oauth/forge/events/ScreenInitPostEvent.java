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
    public static void postScreenOpen(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof JoinMultiplayerScreen multiplayerScreen) {
            JoinMultiplayerScreenHandler.handle(Minecraft.getInstance(), multiplayerScreen);
        }
    }
}
