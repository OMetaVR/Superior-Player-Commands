package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.util.TeleportHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UnstuckCommand {
    
    private static final int SEARCH_RADIUS = 5;
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("unstuck")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(UnstuckCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        World world = player.getWorld();
        BlockPos playerPos = player.getBlockPos();
        
        if (hasSpace(world, playerPos)) {
            source.sendFeedback(() -> Text.literal("You don't appear to be stuck")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        BlockPos safePos = findSafePosition(world, playerPos);
        
        if (safePos == null) {
            source.sendFeedback(() -> Text.literal("No safe position found nearby")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        TeleportHelper.teleportPlayer(player, safePos);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Teleported to safe position")
                .formatted(Formatting.GREEN), false);
        }
        
        return 1;
    }
    
    private static boolean hasSpace(World world, BlockPos pos) {
        return world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir();
    }
    
    private static BlockPos findSafePosition(World world, BlockPos center) {
        for (int radius = 1; radius <= SEARCH_RADIUS; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (Math.abs(x) != radius && Math.abs(y) != radius && Math.abs(z) != radius) {
                            continue;
                        }
                        
                        BlockPos checkPos = center.add(x, y, z);
                        
                        if (TeleportHelper.hasTwoBlockGap(world, checkPos) && 
                            TeleportHelper.hasSolidFloor(world, checkPos)) {
                            return checkPos;
                        }
                    }
                }
            }
        }
        
        return null;
    }
}
