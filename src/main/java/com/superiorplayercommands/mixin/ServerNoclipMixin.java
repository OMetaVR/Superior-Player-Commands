package com.superiorplayercommands.mixin;

import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.EntityPose;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerNoclipMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        
        if (PlayerStateManager.isNoclipEnabled(player.getUuid())) {
            player.noClip = true;
        }
    }
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        
        if (PlayerStateManager.isNoclipEnabled(player.getUuid())) {
            player.noClip = true;
            player.fallDistance = 0;
            player.setPose(EntityPose.STANDING);
        }
    }
    
    @Inject(method = "playerTick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        
        if (PlayerStateManager.isNoclipEnabled(player.getUuid())) {
            player.noClip = true;
            player.fallDistance = 0;
        }
    }
}
