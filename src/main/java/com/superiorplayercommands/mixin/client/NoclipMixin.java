package com.superiorplayercommands.mixin.client;

import com.superiorplayercommands.client.ClientStateManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class NoclipMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        
        if (ClientStateManager.isNoclipEnabled()) {
            player.noClip = true;
            player.setOnGround(false);
            player.fallDistance = 0;
        } else if (player.noClip && !player.isSpectator()) {
            player.noClip = false;
        }
    }
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        
        if (ClientStateManager.isNoclipEnabled()) {
            player.setPose(EntityPose.STANDING);
            player.noClip = true;
        }
    }
    
    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void onTickMovementTail(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        
        if (ClientStateManager.isNoclipEnabled()) {
            player.noClip = true;
            handleNoclipMovement(player);
        }
    }
    
    @Unique
    private void handleNoclipMovement(ClientPlayerEntity player) {
        float forward = player.input.movementForward;
        float strafe = player.input.movementSideways;
        boolean up = player.input.jumping;
        boolean down = player.input.sneaking;
        
        float speed = ClientStateManager.getNoclipSpeed() * 0.5f;
        
        Vec3d velocity = Vec3d.ZERO;
        
        if (forward != 0 || strafe != 0) {
                Vec3d lookVec = player.getRotationVector();
            
            double yawRad = Math.toRadians(player.getYaw());
            Vec3d strafeVec = new Vec3d(Math.cos(yawRad), 0, Math.sin(yawRad));
            
            velocity = velocity.add(lookVec.multiply(forward * speed));
            velocity = velocity.add(strafeVec.multiply(strafe * speed));
        }
        
        if (up) {
            velocity = velocity.add(0, speed, 0);
        }
        if (down) {
            velocity = velocity.add(0, -speed, 0);
        }
        
        player.setVelocity(velocity);
        
        if (!velocity.equals(Vec3d.ZERO)) {
            player.setPosition(player.getPos().add(velocity));
        }
    }
}
