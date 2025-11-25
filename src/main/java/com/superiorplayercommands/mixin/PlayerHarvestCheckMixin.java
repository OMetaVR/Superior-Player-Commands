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
            return; // Don't modify default behavior
        }
        
        ItemStack heldItem = player.getMainHandStack();
        
        // Only apply if not holding a tool
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ToolItem) {
            return; // Let the tool handle it
        }
        
        // Check if the hands tier can harvest this block
        if (!state.isToolRequired()) {
            cir.setReturnValue(true);
            return;
        }
        
        // Check harvest level requirements for various block types
        int requiredLevel = getRequiredHarvestLevel(state);
        
        if (handsLevel.harvestLevel >= requiredLevel) {
            cir.setReturnValue(true);
        }
    }
    
    /**
     * Get the required harvest level for a block
     * 0 = wood/gold, 1 = stone, 2 = iron, 3 = diamond, 4 = netherite
     */
    private int getRequiredHarvestLevel(BlockState state) {
        // Netherite/Ancient Debris level (needs diamond+)
        if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return 3;
        }
        // Diamond ore level (needs iron+)
        if (state.isIn(BlockTags.NEEDS_IRON_TOOL)) {
            return 2;
        }
        // Iron ore level (needs stone+)
        if (state.isIn(BlockTags.NEEDS_STONE_TOOL)) {
            return 1;
        }
        // Default - any tool works
        return 0;
    }
}

