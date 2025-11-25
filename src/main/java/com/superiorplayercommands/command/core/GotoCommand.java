package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.superiorplayercommands.data.WaypointManager;
import com.superiorplayercommands.data.WaypointManager.WaypointData;
import com.superiorplayercommands.util.TeleportHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.Optional;

public class GotoCommand {
    
    private static final SuggestionProvider<ServerCommandSource> WAYPOINT_SUGGESTIONS = (context, builder) -> {
        ServerCommandSource source = context.getSource();
        if (source.isExecutedByPlayer()) {
            return CommandSource.suggestMatching(
                WaypointManager.getWaypointNames(source.getPlayer().getUuid()),
                builder
            );
        }
        return builder.buildFuture();
    };
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("goto")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .suggests(WAYPOINT_SUGGESTIONS)
                    .executes(GotoCommand::execute)
                )
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        String name = StringArgumentType.getString(context, "name");
        
        Optional<WaypointData> waypointOpt = WaypointManager.getWaypoint(player.getUuid(), name);
        
        if (waypointOpt.isEmpty()) {
            source.sendFeedback(() -> Text.literal("Waypoint '")
                .formatted(Formatting.RED)
                .append(Text.literal(name).formatted(Formatting.AQUA))
                .append(Text.literal("' not found").formatted(Formatting.RED)), false);
            return 0;
        }
        
        WaypointData waypoint = waypointOpt.get();
        BlockPos targetPos = waypoint.getPos();
        
        // Handle cross-dimension teleport
        RegistryKey<World> targetDimension = RegistryKey.of(RegistryKeys.WORLD, waypoint.getDimensionId());
        ServerWorld targetWorld = source.getServer().getWorld(targetDimension);
        
        if (targetWorld == null) {
            source.sendFeedback(() -> Text.literal("Cannot teleport: dimension not found")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        // Store previous position for /return (TODO: implement return system)
        
        // Calculate distance before teleporting
        double distance;
        if (player.getWorld().getRegistryKey().equals(targetDimension)) {
            distance = Math.sqrt(player.getBlockPos().getSquaredDistance(targetPos));
        } else {
            distance = -1; // Cross-dimension
        }
        
        // Teleport (handles cross-dimension)
        player.teleport(targetWorld, 
            targetPos.getX() + 0.5, 
            targetPos.getY(), 
            targetPos.getZ() + 0.5, 
            player.getYaw(), 
            player.getPitch());
        
        final double finalDistance = distance;
        if (distance >= 0) {
            source.sendFeedback(() -> Text.literal("Teleported to '")
                .formatted(Formatting.GREEN)
                .append(Text.literal(name).formatted(Formatting.AQUA))
                .append(Text.literal("' (")
                    .formatted(Formatting.GREEN))
                .append(Text.literal(String.format("%.1f blocks", finalDistance))
                    .formatted(Formatting.WHITE))
                .append(Text.literal(")").formatted(Formatting.GREEN)), false);
        } else {
            source.sendFeedback(() -> Text.literal("Teleported to '")
                .formatted(Formatting.GREEN)
                .append(Text.literal(name).formatted(Formatting.AQUA))
                .append(Text.literal("' in ")
                    .formatted(Formatting.GREEN))
                .append(Text.literal(waypoint.getDimensionId().getPath())
                    .formatted(Formatting.LIGHT_PURPLE)), false);
        }
        
        return 1;
    }
}

