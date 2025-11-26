package com.superiorplayercommands.mixin.client;

import com.superiorplayercommands.client.ClientStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public class WaterwalkMixin {
    
    @Unique
    private static final VoxelShape WATERWALK_SHAPE = VoxelShapes.cuboid(0, 0.875, 0, 1, 1, 1);
    
    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionShape(BlockState state, BlockView world, BlockPos pos, 
            ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        
        if (!ClientStateManager.isWaterwalkEnabled()) {
            return;
        }
        
        if (context instanceof EntityShapeContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity instanceof ClientPlayerEntity player) {
                BlockPos above = pos.up();
                BlockState aboveState = world.getBlockState(above);
                boolean isFluidAbove = aboveState.getBlock() instanceof FluidBlock;
                
                if (isFluidAbove) {
                    return;
                }
                
                double playerFeetY = player.getY();
                double fluidTopY = pos.getY() + 1.0;
                
                if (player.isSneaking()) {
                    cir.setReturnValue(VoxelShapes.empty());
                    return;
                }
                
                if (playerFeetY >= pos.getY() + 0.75) {
                    cir.setReturnValue(WATERWALK_SHAPE);
                }
            }
        }
    }
}
