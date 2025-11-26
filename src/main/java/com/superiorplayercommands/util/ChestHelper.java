package com.superiorplayercommands.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ChestHelper {
    
    public static final int SINGLE_CHEST_SIZE = 27;
    public static final int DOUBLE_CHEST_SIZE = 54;
    
    public static class ChestResult {
        public final Inventory inventory;
        public final BlockPos primaryPos;
        public final BlockPos secondaryPos; // null for single chest
        public final boolean isDouble;
        
        public ChestResult(Inventory inventory, BlockPos primaryPos, BlockPos secondaryPos) {
            this.inventory = inventory;
            this.primaryPos = primaryPos;
            this.secondaryPos = secondaryPos;
            this.isDouble = secondaryPos != null;
        }
    }
    
    /**
     * Counts unique item stacks (items that can't be combined)
     */
    public static int countUniqueStacks(List<ItemStack> items) {
        List<ItemStack> uniqueStacks = new ArrayList<>();
        
        for (ItemStack item : items) {
            if (item.isEmpty()) continue;
            
            boolean found = false;
            for (ItemStack existing : uniqueStacks) {
                if (ItemStack.canCombine(existing, item)) {
                    // Check if we can fit more in this stack
                    int space = existing.getMaxCount() - existing.getCount();
                    if (space >= item.getCount()) {
                        existing.increment(item.getCount());
                        found = true;
                        break;
                    } else if (space > 0) {
                        existing.increment(space);
                        item = item.copy();
                        item.decrement(space);
                        // Continue to find another slot or create new
                    }
                }
            }
            
            if (!found && !item.isEmpty()) {
                uniqueStacks.add(item.copy());
            }
        }
        
        return uniqueStacks.size();
    }
    
    /**
     * Determines if we need a double chest based on item count
     */
    public static boolean needsDoubleChest(int stackCount) {
        return stackCount > SINGLE_CHEST_SIZE;
    }
    
    /**
     * Finds or creates appropriate chest(s) for the given number of stacks
     */
    public static ChestResult findOrCreateChest(ServerPlayerEntity player, int stackCount) {
        ServerWorld world = player.getServerWorld();
        BlockPos playerPos = player.getBlockPos();
        boolean needDouble = needsDoubleChest(stackCount);
        
        // First, look for existing chest nearby
        ChestResult existing = findExistingChest(world, playerPos, needDouble);
        if (existing != null) {
            return existing;
        }
        
        // Create new chest(s)
        if (needDouble) {
            return createDoubleChest(world, playerPos, player.getHorizontalFacing());
        } else {
            return createSingleChest(world, playerPos, player.getHorizontalFacing());
        }
    }
    
    private static ChestResult findExistingChest(ServerWorld world, BlockPos center, boolean needDouble) {
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos checkPos = center.add(x, y, z);
                    if (world.getBlockEntity(checkPos) instanceof ChestBlockEntity chest) {
                        BlockState state = world.getBlockState(checkPos);
                        if (state.getBlock() instanceof ChestBlock) {
                            ChestType type = state.get(ChestBlock.CHEST_TYPE);
                            
                            if (type != ChestType.SINGLE) {
                                // It's already a double chest
                                Direction facing = ChestBlock.getFacing(state);
                                BlockPos otherPos = checkPos.offset(facing);
                                if (world.getBlockEntity(otherPos) instanceof ChestBlockEntity otherChest) {
                                    Inventory doubleInv = new DoubleInventory(chest, otherChest);
                                    return new ChestResult(doubleInv, checkPos, otherPos);
                                }
                            } else if (!needDouble) {
                                // Single chest is fine
                                return new ChestResult(chest, checkPos, null);
                            }
                            // If we need double but found single, keep looking or we'll create new
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private static ChestResult createSingleChest(ServerWorld world, BlockPos playerPos, Direction facing) {
        // Try positions in order: in front, sides, behind, then up/down variations
        Direction[] tryDirs = {facing, facing.rotateYClockwise(), facing.rotateYCounterclockwise(), facing.getOpposite()};
        
        for (Direction dir : tryDirs) {
            BlockPos pos = playerPos.offset(dir, 2);
            if (isValidSingleChestPosition(world, pos)) {
                world.setBlockState(pos, Blocks.CHEST.getDefaultState());
                if (world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                    return new ChestResult(chest, pos, null);
                }
            }
        }
        
        // Try one block up
        for (Direction dir : tryDirs) {
            BlockPos pos = playerPos.up().offset(dir, 2);
            if (isValidSingleChestPosition(world, pos)) {
                world.setBlockState(pos, Blocks.CHEST.getDefaultState());
                if (world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                    return new ChestResult(chest, pos, null);
                }
            }
        }
        
        return null;
    }
    
    private static ChestResult createDoubleChest(ServerWorld world, BlockPos playerPos, Direction facing) {
        // For double chest, we need a 2x1 horizontal area
        // Try placing perpendicular to facing direction for better visibility
        Direction[] tryDirs = {facing, facing.rotateYClockwise(), facing.rotateYCounterclockwise(), facing.getOpposite()};
        
        for (Direction dir : tryDirs) {
            BlockPos pos1 = playerPos.offset(dir, 2);
            // Try placing second chest to the left or right
            for (Direction sideDir : new Direction[]{dir.rotateYClockwise(), dir.rotateYCounterclockwise()}) {
                BlockPos pos2 = pos1.offset(sideDir);
                
                if (isValidDoubleChestPosition(world, pos1, pos2)) {
                    // Place first chest - swap LEFT/RIGHT based on side direction
                    BlockState chestState1 = Blocks.CHEST.getDefaultState()
                        .with(ChestBlock.FACING, dir.getOpposite())
                        .with(ChestBlock.CHEST_TYPE, sideDir == dir.rotateYClockwise() ? ChestType.RIGHT : ChestType.LEFT);
                    world.setBlockState(pos1, chestState1);
                    
                    // Place second chest
                    BlockState chestState2 = Blocks.CHEST.getDefaultState()
                        .with(ChestBlock.FACING, dir.getOpposite())
                        .with(ChestBlock.CHEST_TYPE, sideDir == dir.rotateYClockwise() ? ChestType.LEFT : ChestType.RIGHT);
                    world.setBlockState(pos2, chestState2);
                    
                    if (world.getBlockEntity(pos1) instanceof ChestBlockEntity chest1 &&
                        world.getBlockEntity(pos2) instanceof ChestBlockEntity chest2) {
                        Inventory doubleInv = new DoubleInventory(chest1, chest2);
                        return new ChestResult(doubleInv, pos1, pos2);
                    }
                }
            }
        }
        
        // Try one block up
        for (Direction dir : tryDirs) {
            BlockPos pos1 = playerPos.up().offset(dir, 2);
            for (Direction sideDir : new Direction[]{dir.rotateYClockwise(), dir.rotateYCounterclockwise()}) {
                BlockPos pos2 = pos1.offset(sideDir);
                
                if (isValidDoubleChestPosition(world, pos1, pos2)) {
                    BlockState chestState1 = Blocks.CHEST.getDefaultState()
                        .with(ChestBlock.FACING, dir.getOpposite())
                        .with(ChestBlock.CHEST_TYPE, sideDir == dir.rotateYClockwise() ? ChestType.RIGHT : ChestType.LEFT);
                    world.setBlockState(pos1, chestState1);
                    
                    BlockState chestState2 = Blocks.CHEST.getDefaultState()
                        .with(ChestBlock.FACING, dir.getOpposite())
                        .with(ChestBlock.CHEST_TYPE, sideDir == dir.rotateYClockwise() ? ChestType.LEFT : ChestType.RIGHT);
                    world.setBlockState(pos2, chestState2);
                    
                    if (world.getBlockEntity(pos1) instanceof ChestBlockEntity chest1 &&
                        world.getBlockEntity(pos2) instanceof ChestBlockEntity chest2) {
                        Inventory doubleInv = new DoubleInventory(chest1, chest2);
                        return new ChestResult(doubleInv, pos1, pos2);
                    }
                }
            }
        }
        
        return null;
    }
    
    private static boolean isValidSingleChestPosition(World world, BlockPos pos) {
        // Check the position is air
        if (!world.getBlockState(pos).isAir()) {
            return false;
        }
        // Check above is not solid opaque (chest needs to open)
        BlockPos above = pos.up();
        BlockState aboveState = world.getBlockState(above);
        if (!aboveState.isAir() && aboveState.isOpaque()) {
            return false;
        }
        return true;
    }
    
    private static boolean isValidDoubleChestPosition(World world, BlockPos pos1, BlockPos pos2) {
        // Both positions must be valid
        if (!isValidSingleChestPosition(world, pos1)) return false;
        if (!isValidSingleChestPosition(world, pos2)) return false;
        return true;
    }
    
    /**
     * Stores items in the chest inventory, returns count of items stored
     */
    public static int storeItems(Inventory chest, List<ItemStack> items) {
        int stored = 0;
        
        for (ItemStack item : items) {
            if (item.isEmpty()) continue;
            
            ItemStack toStore = item.copy();
            
            // First try to merge with existing stacks
            for (int i = 0; i < chest.size() && !toStore.isEmpty(); i++) {
                ItemStack slotStack = chest.getStack(i);
                if (ItemStack.canCombine(slotStack, toStore) && slotStack.getCount() < slotStack.getMaxCount()) {
                    int space = slotStack.getMaxCount() - slotStack.getCount();
                    int toAdd = Math.min(space, toStore.getCount());
                    slotStack.increment(toAdd);
                    toStore.decrement(toAdd);
                }
            }
            
            // Then try empty slots
            for (int i = 0; i < chest.size() && !toStore.isEmpty(); i++) {
                if (chest.getStack(i).isEmpty()) {
                    chest.setStack(i, toStore.copy());
                    toStore = ItemStack.EMPTY;
                }
            }
            
            if (toStore.isEmpty() || toStore.getCount() < item.getCount()) {
                stored++;
            }
        }
        
        return stored;
    }
    
    /**
     * Drops any overflow items near the chest
     */
    public static void dropOverflow(World world, BlockPos chestPos, List<ItemStack> items, Inventory chest) {
        for (ItemStack item : items) {
            if (item.isEmpty()) continue;
            
            ItemStack remaining = item.copy();
            
            // Try to store
            for (int i = 0; i < chest.size() && !remaining.isEmpty(); i++) {
                ItemStack slotStack = chest.getStack(i);
                if (slotStack.isEmpty()) {
                    chest.setStack(i, remaining.copy());
                    remaining = ItemStack.EMPTY;
                } else if (ItemStack.canCombine(slotStack, remaining) && slotStack.getCount() < slotStack.getMaxCount()) {
                    int space = slotStack.getMaxCount() - slotStack.getCount();
                    int toAdd = Math.min(space, remaining.getCount());
                    slotStack.increment(toAdd);
                    remaining.decrement(toAdd);
                }
            }
            
            // Drop overflow
            if (!remaining.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(world,
                    chestPos.getX() + 0.5, chestPos.getY() + 1, chestPos.getZ() + 0.5, remaining);
                world.spawnEntity(itemEntity);
            }
        }
    }
}
