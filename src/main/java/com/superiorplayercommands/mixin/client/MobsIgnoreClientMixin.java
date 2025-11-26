package com.superiorplayercommands.mixin.client;

import com.superiorplayercommands.client.ClientStateManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobsIgnoreClientMixin {
    
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void onSetTarget(LivingEntity target, CallbackInfo ci) {
        if (target instanceof ClientPlayerEntity) {
            if (ClientStateManager.isMobsIgnoreEnabled() || ClientStateManager.isNoclipEnabled()) {
                ci.cancel();
            }
        }
    }
}
