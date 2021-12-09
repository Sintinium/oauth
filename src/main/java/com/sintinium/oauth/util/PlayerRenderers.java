package com.sintinium.oauth.util;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;

public class PlayerRenderers {

    public static PlayerModel<AbstractClientPlayerEntity> playerModel = new PlayerModel<>(0f, false);
    public static PlayerModel<AbstractClientPlayerEntity> slimModel = new PlayerModel<>(0f, true);

}
