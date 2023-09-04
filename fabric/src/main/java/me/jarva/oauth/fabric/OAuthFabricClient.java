package me.jarva.oauth.fabric;

import me.jarva.oauth.OAuthClient;
import me.jarva.oauth.fabric.events.ScreenInitPostEvent;
import net.fabricmc.api.ClientModInitializer;

public class OAuthFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OAuthClient.init();

        ScreenInitPostEvent.register();
    }
}
