package com.superiorplayercommands.mixin;

import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class JumpBoostMixin {
    
    @Inject(method = "jump", at = @At("TAIL"))
    private void onJump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (entity instanceof ServerPlayerEntity player) {
            float multiplier = PlayerStateManager.getJumpMultiplier(player.getUuid());
            
            if (multiplier != 1.0f) {
                Vec3d velocity = player.getVelocity();
                // Base jump velocity is about 0.42, multiply the Y component
                double extraJump = velocity.y * (multiplier - 1.0);
                player.setVelocity(velocity.x, velocity.y + extraJump, velocity.z);
                player.velocityModified = true;
            }
        }
    }
}
