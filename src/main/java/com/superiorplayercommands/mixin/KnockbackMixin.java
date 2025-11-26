package com.superiorplayercommands.mixin;

import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class KnockbackMixin {
    
    @ModifyVariable(method = "takeKnockback", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double modifyKnockbackStrength(double strength) {
        LivingEntity self = (LivingEntity) (Object) this;
        Entity attacker = self.getAttacker();
        
        if (attacker instanceof ServerPlayerEntity player) {
            float multiplier = PlayerStateManager.getKnockbackMultiplier(player.getUuid());
            return strength * multiplier;
        }
        
        return strength;
    }
}
