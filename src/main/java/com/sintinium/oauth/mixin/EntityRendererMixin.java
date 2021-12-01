package com.sintinium.oauth.mixin;

import com.sintinium.oauth.mixin.constants.PlayerRenderers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityRenderers.class)
public class EntityRendererMixin {

    @Inject(method = "createPlayerRenderers", at = @At("HEAD"))
    private static void onCreatePlayerRenderers(EntityRendererProvider.Context p_174052_, CallbackInfoReturnable<Map<String, EntityRenderer<? extends Player>>> cir) {
        PlayerRenderers.createPlayerRenderers(p_174052_);
    }

}
