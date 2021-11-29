package com.sintinium.oauth.mixin;

import com.sintinium.oauth.gui.profile.FakePlayer;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRendererManager.class)
public abstract class EntityRendererManagerMixin {

    private final PlayerRenderer fakePlayerRenderer = new PlayerRenderer((EntityRendererManager) (Object) this);
    private final PlayerRenderer fakePlayerRendererSlim = new PlayerRenderer((EntityRendererManager) (Object) this, true);

    // Replace the player renderer with our own. This is to stop other mods from attempting to alter the player before it exists.
    // The main fix is for the hats mod
    @Inject(method = "getRenderer", at = @At("RETURN"), cancellable = true)
    public <T extends Entity> void onGetRenderer(T pEntity, CallbackInfoReturnable<EntityRenderer<? super T>> cir) {
        if (!(pEntity instanceof FakePlayer)) return;
        String s = ((AbstractClientPlayerEntity) pEntity).getModelName();
        PlayerRenderer playerrenderer = null;
        if (s.equals("default")) playerrenderer = this.fakePlayerRenderer;
        else if (s.equals("slim")) playerrenderer = this.fakePlayerRendererSlim;
        cir.setReturnValue((EntityRenderer<? super T>) playerrenderer);
    }
}
