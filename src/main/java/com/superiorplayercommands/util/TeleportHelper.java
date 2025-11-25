package com.superiorplayercommands.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Helper class for teleportation-related operations
 */
public class TeleportHelper {
    
    /**
     * Check if a block is passable (air, transparent, or non-solid)
     */
    public static boolean isPassable(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        // Air, plants, torches, etc. - anything you can walk through
        return !state.blocksMovement();
    }
    
    /**
     * Check if a block is a liquid (water or lava)
     */
    public static boolean isLiquid(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.getFluidState().isEmpty();
    }
    
    /**
     * Check if a block is water
     */
    public static boolean isWater(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isOf(Blocks.WATER);
    }
    
    /**
     * Check if a block is lava
     */
    public static boolean isLava(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isOf(Blocks.LAVA);
    }
    
    /**
     * Check if a position has a 2-block tall gap for a player to stand
     */
    public static boolean hasTwoBlockGap(World world, BlockPos feetPos) {
        return isPassable(world, feetPos) && isPassable(world, feetPos.up());
    }
    
    /**
     * Check if a position has a solid floor
     */
    public static boolean hasSolidFloor(World world, BlockPos feetPos) {
        BlockState below = world.getBlockState(feetPos.down());
        return below.blocksMovement() && !isLiquid(world, feetPos.down());
    }
    
    /**
     * Result of a scan operation
     */
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
    
    /**
     * Scan upward from the player's position to find the first valid 2-block gap
     * Returns the feet position of where the player should stand
     */
    public static ScanResult scanUp(World world, BlockPos startPos) {
        int maxY = world.getTopY();
        
        // Start scanning from one block above the player's head
        BlockPos scanPos = startPos.up(2);
        
        while (scanPos.getY() < maxY - 1) {
            // Check if we have a 2-block gap here
            if (hasTwoBlockGap(world, scanPos)) {
                // Check if there's a floor below this gap
                if (hasSolidFloor(world, scanPos)) {
                    // Check for liquids at the destination
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
    
    /**
     * Scan downward from the player's position to find the first valid 2-block gap with a floor
     * Returns the feet position of where the player should stand
     */
    public static ScanResult scanDown(World world, BlockPos startPos) {
        int minY = world.getBottomY();
        
        // Skip past the floor the player is currently standing on
        // Start by going down 2 blocks (below player's feet floor)
        BlockPos scanPos = startPos.down(2);
        
        while (scanPos.getY() > minY) {
            // First, find a solid block (potential floor)
            BlockState state = world.getBlockState(scanPos);
            
            if (state.blocksMovement() && !isLiquid(world, scanPos)) {
                // Found a solid block, check if there's a 2-block gap above it
                BlockPos feetPos = scanPos.up();
                if (hasTwoBlockGap(world, feetPos)) {
                    // Check for liquids at the destination
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
    
    /**
     * Check for liquids at a position (feet and head level)
     */
    private static LiquidType checkLiquidAt(World world, BlockPos feetPos) {
        // Check feet and head position
        if (isLava(world, feetPos) || isLava(world, feetPos.up())) {
            return LiquidType.LAVA;
        }
        if (isWater(world, feetPos) || isWater(world, feetPos.up())) {
            return LiquidType.WATER;
        }
        return LiquidType.NONE;
    }
    
    /**
     * Teleport a player to a position (feet position)
     */
    public static void teleportPlayer(ServerPlayerEntity player, BlockPos feetPos) {
        player.teleport(
            feetPos.getX() + 0.5,  // Center of block
            feetPos.getY(),
            feetPos.getZ() + 0.5   // Center of block
        );
    }
}

