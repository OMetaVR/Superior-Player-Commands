package com.superiorplayercommands.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportHelper {
    
    public static boolean isPassable(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.blocksMovement();
    }
    
    public static boolean isLiquid(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.getFluidState().isEmpty();
    }
    
    public static boolean isWater(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isOf(Blocks.WATER);
    }
    
    public static boolean isLava(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isOf(Blocks.LAVA);
    }
    
    public static boolean hasTwoBlockGap(World world, BlockPos feetPos) {
        return isPassable(world, feetPos) && isPassable(world, feetPos.up());
    }
    
    public static boolean hasSolidFloor(World world, BlockPos feetPos) {
        BlockState below = world.getBlockState(feetPos.down());
        return below.blocksMovement() && !isLiquid(world, feetPos.down());
    }
    
    public static class ScanResult {
        public final boolean found;
        public final BlockPos position;
        public final LiquidType liquidFound;
        
        public ScanResult(boolean found, BlockPos position, LiquidType liquidFound) {
            this.found = found;
            this.position = position;
            this.liquidFound = liquidFound;
        }
        
        public static ScanResult notFound() {
            return new ScanResult(false, null, LiquidType.NONE);
        }
        
        public static ScanResult success(BlockPos pos) {
            return new ScanResult(true, pos, LiquidType.NONE);
        }
        
        public static ScanResult liquidWarning(BlockPos pos, LiquidType liquid) {
            return new ScanResult(true, pos, liquid);
        }
    }
    
    public enum LiquidType {
        NONE,
        WATER,
        LAVA
    }
    
    public static ScanResult scanUp(World world, BlockPos startPos) {
        int maxY = world.getTopY();
        BlockPos scanPos = startPos.up(2);
        
        while (scanPos.getY() < maxY - 1) {
            if (hasTwoBlockGap(world, scanPos)) {
                if (hasSolidFloor(world, scanPos)) {
                    LiquidType liquid = checkLiquidAt(world, scanPos);
                    if (liquid != LiquidType.NONE) {
                        return ScanResult.liquidWarning(scanPos, liquid);
                    }
                    return ScanResult.success(scanPos);
                }
            }
            scanPos = scanPos.up();
        }
        
        return ScanResult.notFound();
    }
    
    public static ScanResult scanDown(World world, BlockPos startPos) {
        int minY = world.getBottomY();
        BlockPos scanPos = startPos.down(2);
        
        while (scanPos.getY() > minY) {
            BlockState state = world.getBlockState(scanPos);
            
            if (state.blocksMovement() && !isLiquid(world, scanPos)) {
                BlockPos feetPos = scanPos.up();
                if (hasTwoBlockGap(world, feetPos)) {
                    LiquidType liquid = checkLiquidAt(world, feetPos);
                    if (liquid != LiquidType.NONE) {
                        return ScanResult.liquidWarning(feetPos, liquid);
                    }
                    return ScanResult.success(feetPos);
                }
            }
            scanPos = scanPos.down();
        }
        
        return ScanResult.notFound();
    }
    
    private static LiquidType checkLiquidAt(World world, BlockPos feetPos) {
        if (isLava(world, feetPos) || isLava(world, feetPos.up())) {
            return LiquidType.LAVA;
        }
        if (isWater(world, feetPos) || isWater(world, feetPos.up())) {
            return LiquidType.WATER;
        }
        return LiquidType.NONE;
    }
    
    public static void teleportPlayer(ServerPlayerEntity player, BlockPos feetPos) {
        player.teleport(
            feetPos.getX() + 0.5,
            feetPos.getY(),
            feetPos.getZ() + 0.5
        );
    }
}

