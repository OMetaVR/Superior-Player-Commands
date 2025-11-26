package com.superiorplayercommands.mixin;

import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.data.PlayerStateManager.ToolTier;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.tag.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerHarvestCheckMixin {
    
    @Inject(method = "canHarvest", at = @At("HEAD"), cancellable = true)
    private void onCanHarvest(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        ToolTier handsLevel = PlayerStateManager.getHandsLevel(player.getUuid());
        
        if (handsLevel == ToolTier.NONE) {
            return;
        }
        
        ItemStack heldItem = player.getMainHandStack();
        
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ToolItem) {
            return;
        }
        
        if (!state.isToolRequired()) {
            cir.setReturnValue(true);
            return;
        }
        
        int requiredLevel = getRequiredHarvestLevel(state);
        
        if (handsLevel.harvestLevel >= requiredLevel) {
            cir.setReturnValue(true);
        }
    }
    
    private int getRequiredHarvestLevel(BlockState state) {
        if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return 3;
        }
        if (state.isIn(BlockTags.NEEDS_IRON_TOOL)) {
            return 2;
        }
        if (state.isIn(BlockTags.NEEDS_STONE_TOOL)) {
            return 1;
        }
        return 0;
    }
}



