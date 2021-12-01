package com.sintinium.oauth.mixin.constants;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

public class PlayerRenderers {
    public static PlayerRenderer fakePlayerRenderer = null;
    public static PlayerRenderer fakePlayerRendererSlim = null;

    public static void createPlayerRenderers(EntityRendererProvider.Context context) {
        fakePlayerRenderer = new PlayerRenderer(context, false);
        fakePlayerRendererSlim = new PlayerRenderer(context, true);
    }
}
