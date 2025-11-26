package com.superiorplayercommands.command.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExtinguishCommand {
    
    private static final int DEFAULT_RADIUS = 128; // Roughly render distance
    private static final int MAX_RADIUS = 256;
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("extinguish")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> execute(context, DEFAULT_RADIUS))
                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                    .executes(context -> execute(context, IntegerArgumentType.getInteger(context, "radius")))
                )
        );
        
        dispatcher.register(
            CommandManager.literal("ext")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> execute(context, DEFAULT_RADIUS))
                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                    .executes(context -> execute(context, IntegerArgumentType.getInteger(context, "radius")))
                )
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context, int radius) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        World world = player.getWorld();
        BlockPos center = player.getBlockPos();
        
        int extinguished = 0;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    
                    if (world.getBlockState(pos).getBlock() == Blocks.FIRE ||
                        world.getBlockState(pos).getBlock() == Blocks.SOUL_FIRE) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        extinguished++;
                    }
                }
            }
        }
        
        final int count = extinguished;
        if (count > 0) {
            source.sendFeedback(() -> Text.literal("Extinguished ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(count))
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" fire" + (count != 1 ? "s" : ""))
                    .formatted(Formatting.GREEN)), false);
        } else {
            source.sendFeedback(() -> Text.literal("No fires found within " + radius + " blocks")
                .formatted(Formatting.GRAY), false);
        }
        
        return count;
    }
}
