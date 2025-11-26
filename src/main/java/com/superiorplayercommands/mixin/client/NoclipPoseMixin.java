package com.superiorplayercommands.mixin.client;

import com.superiorplayercommands.client.ClientStateManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class NoclipPoseMixin {
    
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self instanceof ClientPlayerEntity) {
            if (ClientStateManager.isNoclipEnabled()) {
                cir.setReturnValue(PlayerEntity.STANDING_DIMENSIONS);
            }
        }
    }
}
