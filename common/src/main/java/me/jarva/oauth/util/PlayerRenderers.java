package me.jarva.oauth.util;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

public class PlayerRenderers {
    public static PlayerRenderer fakePlayerRenderer = null;
    public static PlayerRenderer fakePlayerRendererSlim = null;
    public static PlayerModel<AbstractClientPlayer> playerModel;
    public static PlayerModel<AbstractClientPlayer> slimPlayerModel;

    public static void createPlayerRenderers(EntityRendererProvider.Context context) {
        fakePlayerRenderer = new PlayerRenderer(context, false);
        fakePlayerRendererSlim = new PlayerRenderer(context, true);
        playerModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        slimPlayerModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }
}
