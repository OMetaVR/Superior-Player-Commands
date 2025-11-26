package com.superiorplayercommands.mixin;

import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MovementValidationMixin {
    
    @Shadow
    public ServerPlayerEntity player;
    
    @Inject(method = "onPlayerMove", at = @At("HEAD"))
    private void onPlayerMoveHead(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (PlayerStateManager.isNoclipEnabled(player.getUuid())) {
            player.noClip = true;
        }
    }
}
