package com.sintinium.oauth.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.sintinium.oauth.gui.profile.FakePlayer;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

    public PlayerRendererMixin(EntityRendererManager p_i50965_1_, PlayerModel<AbstractClientPlayerEntity> p_i50965_2_, float p_i50965_3_) {
        super(p_i50965_1_, p_i50965_2_, p_i50965_3_);
    }

    @Shadow
    protected abstract void setModelProperties(AbstractClientPlayerEntity pClientPlayer);

    // Bypass forge events to prevent errors with other mods
    @Inject(method = "render(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", at = @At("HEAD"), cancellable = true)
    public void onRender(AbstractClientPlayerEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, CallbackInfo ci) {
        if (!(pEntity instanceof FakePlayer)) return;
        ci.cancel();
        this.setModelProperties(pEntity);
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

}
