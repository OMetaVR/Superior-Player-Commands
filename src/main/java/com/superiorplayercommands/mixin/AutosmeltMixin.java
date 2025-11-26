package com.superiorplayercommands.mixin;

import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(Block.class)
public class AutosmeltMixin {
    
    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;", 
            at = @At("RETURN"), cancellable = true)
    private static void onGetDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, 
            @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, 
            CallbackInfoReturnable<List<ItemStack>> cir) {
        
        if (entity instanceof ServerPlayerEntity player) {
            if (PlayerStateManager.isAutosmeltEnabled(player.getUuid())) {
                List<ItemStack> originalDrops = cir.getReturnValue();
                List<ItemStack> smeltedDrops = new ArrayList<>();
                
                for (ItemStack drop : originalDrops) {
                    ItemStack smelted = getSmeltedResult(world, drop);
                    if (smelted != null) {
                        smelted.setCount(drop.getCount());
                        smeltedDrops.add(smelted);
                    } else {
                        smeltedDrops.add(drop);
                    }
                }
                
                cir.setReturnValue(smeltedDrops);
            }
        }
    }
    
    private static ItemStack getSmeltedResult(ServerWorld world, ItemStack input) {
        Optional<SmeltingRecipe> recipe = world.getRecipeManager()
            .getFirstMatch(RecipeType.SMELTING, 
                new net.minecraft.inventory.SimpleInventory(input), 
                world);
        
        if (recipe.isPresent()) {
            return recipe.get().getOutput(world.getRegistryManager()).copy();
        }
        
        return null;
    }
}
