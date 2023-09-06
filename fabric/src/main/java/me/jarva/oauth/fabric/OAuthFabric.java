package me.jarva.oauth.fabric;

import me.jarva.oauth.OAuth;
import net.fabricmc.api.ModInitializer;

public class OAuthFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        OAuth.init();
    }
}
