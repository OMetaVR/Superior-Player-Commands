package com.superiorplayercommands.mixin;

import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class BlockBreakingMixin {
    
    @Inject(method = "calcBlockBreakingDelta", at = @At("HEAD"), cancellable = true)
    private void onCalcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        // Check for instamine (with cooldown)
        if (PlayerStateManager.isInstamineReady(player.getUuid())) {
            // Return 1.0f = instant break (unless unbreakable)
            if (state.getHardness(world, pos) >= 0) {
                cir.setReturnValue(1.0f);
                return;
            }
        } else if (PlayerStateManager.isInstamineEnabled(player.getUuid())) {
            // Instamine enabled but on cooldown - return very slow speed to prevent breaking
            cir.setReturnValue(0.0f);
            return;
        }
        
        // Check for tool hands
        PlayerStateManager.ToolTier handsLevel = PlayerStateManager.getHandsLevel(player.getUuid());
        if (handsLevel != PlayerStateManager.ToolTier.NONE) {
            ItemStack heldItem = player.getMainHandStack();
            
            // Only apply if not already holding a tool
            if (heldItem.isEmpty() || !(heldItem.getItem() instanceof ToolItem)) {
                float hardness = state.getHardness(world, pos);
                if (hardness < 0) {
                    cir.setReturnValue(0.0f); // Unbreakable
                    return;
                }
                
                // Calculate mining speed based on hands level
                float speed = handsLevel.getMiningSpeed();
                
                // Apply efficiency-like boost
                float delta = speed / hardness / 30.0f;
                cir.setReturnValue(delta);
            }
        }
    }
}

