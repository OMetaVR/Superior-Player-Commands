package com.superiorplayercommands.mixin;

import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class FreezeAIMixin {
    
    @Inject(method = "isAiDisabled", at = @At("HEAD"), cancellable = true)
    private void onIsAiDisabled(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerStateManager.isFreezeAI()) {
            cir.setReturnValue(true);
        }
    }
}
