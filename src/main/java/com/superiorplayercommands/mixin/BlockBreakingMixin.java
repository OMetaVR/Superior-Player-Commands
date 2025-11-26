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
        if (PlayerStateManager.isInstamineEnabled(player.getUuid())) {
            if (state.getHardness(world, pos) >= 0) {
                long packedPos = pos.asLong();
                if (PlayerStateManager.isInstamineReady(player.getUuid(), packedPos)) {
                    PlayerStateManager.updateInstaminePosition(player.getUuid(), packedPos);
                    cir.setReturnValue(1.0f);
                } else {
                    cir.setReturnValue(0.0001f);
                }
                return;
            }
        }
        
        PlayerStateManager.ToolTier handsLevel = PlayerStateManager.getHandsLevel(player.getUuid());
        if (handsLevel != PlayerStateManager.ToolTier.NONE) {
            ItemStack heldItem = player.getMainHandStack();
            
            if (heldItem.isEmpty() || !(heldItem.getItem() instanceof ToolItem)) {
                float hardness = state.getHardness(world, pos);
                if (hardness < 0) {
                    cir.setReturnValue(0.0f);
                    return;
                }
                
                float speed = handsLevel.getMiningSpeed();
                float delta = speed / hardness / 30.0f;
                cir.setReturnValue(delta);
            }
        }
    }
}

