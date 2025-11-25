package com.superiorplayercommands.mixin;

import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(Block.class)
public class BlockDropsMixin {
    
    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;", 
            at = @At("HEAD"), cancellable = true)
    private static void onGetDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, 
            @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, 
            CallbackInfoReturnable<List<ItemStack>> cir) {
        
        if (entity instanceof PlayerEntity player) {
            if (!PlayerStateManager.areDropsEnabled(player.getUuid())) {
                // Return empty list - no drops
                cir.setReturnValue(Collections.emptyList());
            }
        }
    }
}

