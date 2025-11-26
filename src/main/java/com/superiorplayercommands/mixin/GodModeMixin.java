package com.superiorplayercommands.mixin;

import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class GodModeMixin {
    
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        
        // Full god mode or noclip - block all damage
        if (PlayerStateManager.isGodModeEnabled(player.getUuid()) ||
            PlayerStateManager.isNoclipEnabled(player.getUuid())) {
            cir.setReturnValue(false);
            return;
        }
        
        // Check specific damage type toggles
        if (!PlayerStateManager.isFallDamageEnabled(player.getUuid())) {
            if (source.isOf(DamageTypes.FALL)) {
                cir.setReturnValue(false);
                return;
            }
        }
        
        if (!PlayerStateManager.isFireDamageEnabled(player.getUuid())) {
            if (source.isOf(DamageTypes.IN_FIRE) || 
                source.isOf(DamageTypes.ON_FIRE) || 
                source.isOf(DamageTypes.LAVA) ||
                source.isOf(DamageTypes.HOT_FLOOR)) {
                cir.setReturnValue(false);
                return;
            }
        }
        
        if (!PlayerStateManager.isDrownDamageEnabled(player.getUuid())) {
            if (source.isOf(DamageTypes.DROWN)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
