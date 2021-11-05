package com.sintinium.oauth.mixin;

import com.sintinium.oauth.gui.profile.FakePlayer;
import com.sintinium.oauth.gui.profile.ProfileScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingRenderer.class)
public abstract class LivingRendererMixin<T extends LivingEntity, M extends EntityModel<T>> implements IEntityRenderer<T, M> {

    @Inject(method = "shouldShowName(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("INVOKE"), remap = false, cancellable = true)
    public void shouldShowNameMixin(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof FakePlayer || Minecraft.getInstance().screen instanceof ProfileScreen) cir.setReturnValue(false);
    }

}
