package com.superiorplayercommands.command.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FreezeCommand {
    
    private static final int DEFAULT_RADIUS = 32;
    private static final int MAX_RADIUS = 128;
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            dispatcher.register(
            CommandManager.literal("snow")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> executeFreeze(context, DEFAULT_RADIUS))
                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                    .executes(context -> executeFreeze(context, IntegerArgumentType.getInteger(context, "radius")))
                )
        );
        
        dispatcher.register(
            CommandManager.literal("thaw")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> executeThaw(context, DEFAULT_RADIUS))
                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                    .executes(context -> executeThaw(context, IntegerArgumentType.getInteger(context, "radius")))
                )
        );
    }
    
    private static int executeFreeze(CommandContext<ServerCommandSource> context, int radius) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        World world = player.getWorld();
        BlockPos center = player.getBlockPos();
        
        int frozen = 0;
        int snowed = 0;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    
                    if (state.getBlock() == Blocks.WATER) {
                        world.setBlockState(pos, Blocks.ICE.getDefaultState());
                        frozen++;
                    } else if (state.isSolidBlock(world, pos) && 
                             world.getBlockState(pos.up()).isAir() &&
                             world.isSkyVisible(pos.up())) {
                        world.setBlockState(pos.up(), Blocks.SNOW.getDefaultState());
                        snowed++;
                    }
                }
            }
        }
        
        final int frozenCount = frozen;
        final int snowedCount = snowed;
        
        if (frozen > 0 || snowed > 0) {
            source.sendFeedback(() -> Text.literal("Frozen ")
                .formatted(Formatting.AQUA)
                .append(Text.literal(String.valueOf(frozenCount))
                    .formatted(Formatting.WHITE))
                .append(Text.literal(" water blocks, added ")
                    .formatted(Formatting.AQUA))
                .append(Text.literal(String.valueOf(snowedCount))
                    .formatted(Formatting.WHITE))
                .append(Text.literal(" snow")
                    .formatted(Formatting.AQUA)), false);
        } else {
            source.sendFeedback(() -> Text.literal("Nothing to freeze within " + radius + " blocks")
                .formatted(Formatting.GRAY), false);
        }
        
        return frozen + snowed;
    }
    
    private static int executeThaw(CommandContext<ServerCommandSource> context, int radius) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        World world = player.getWorld();
        BlockPos center = player.getBlockPos();
        
        int thawed = 0;
        int snowRemoved = 0;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    
                    if (state.getBlock() == Blocks.ICE || 
                        state.getBlock() == Blocks.PACKED_ICE ||
                        state.getBlock() == Blocks.BLUE_ICE ||
                        state.getBlock() == Blocks.FROSTED_ICE) {
                        world.setBlockState(pos, Blocks.WATER.getDefaultState());
                        thawed++;
                    } else if (state.getBlock() == Blocks.SNOW || 
                             state.getBlock() == Blocks.SNOW_BLOCK ||
                             state.getBlock() == Blocks.POWDER_SNOW) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        snowRemoved++;
                    }
                }
            }
        }
        
        final int thawedCount = thawed;
        final int snowCount = snowRemoved;
        
        if (thawed > 0 || snowRemoved > 0) {
            source.sendFeedback(() -> Text.literal("Thawed ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal(String.valueOf(thawedCount))
                    .formatted(Formatting.WHITE))
                .append(Text.literal(" ice blocks, removed ")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal(String.valueOf(snowCount))
                    .formatted(Formatting.WHITE))
                .append(Text.literal(" snow")
                    .formatted(Formatting.YELLOW)), false);
        } else {
            source.sendFeedback(() -> Text.literal("Nothing to thaw within " + radius + " blocks")
                .formatted(Formatting.GRAY), false);
        }
        
        return thawed + snowRemoved;
    }
}
