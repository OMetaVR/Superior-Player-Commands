package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.data.WaypointManager;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class RemCommand {
    
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
            CommandManager.literal("rem")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(RemCommand::executeNoArg)
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .suggests(WAYPOINT_SUGGESTIONS)
                    .executes(RemCommand::execute)
                )
        );
    }
    
    private static int executeNoArg(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        BlockPos playerPos = player.getBlockPos();
        String currentDimension = player.getWorld().getRegistryKey().getValue().toString();
        
        Map<String, WaypointManager.WaypointData> waypoints = WaypointManager.getWaypoints(player.getUuid());
        
        for (Map.Entry<String, WaypointManager.WaypointData> entry : waypoints.entrySet()) {
            WaypointManager.WaypointData data = entry.getValue();
            if (data.dimension.equals(currentDimension)) {
                BlockPos waypointPos = data.getPos();
                if (playerPos.isWithinDistance(waypointPos, 2.0)) {
                    String name = entry.getKey();
                    WaypointManager.removeWaypoint(player.getUuid(), name);
                    
                    if (!PlayerStateManager.isHideResponses(player.getUuid())) {
                        source.sendFeedback(() -> Text.literal("Removed waypoint '")
                            .formatted(Formatting.YELLOW)
                            .append(Text.literal(name).formatted(Formatting.AQUA))
                            .append(Text.literal("'").formatted(Formatting.YELLOW)), false);
                    }
                    return 1;
                }
            }
        }
        
        source.sendFeedback(() -> Text.literal("Please specify a waypoint name: ")
            .formatted(Formatting.YELLOW)
            .append(Text.literal("/rem <name>").formatted(Formatting.AQUA)), false);
        
        if (!waypoints.isEmpty()) {
            source.sendFeedback(() -> Text.literal("Your waypoints: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(String.join(", ", waypoints.keySet()))
                    .formatted(Formatting.WHITE)), false);
        }
        
        return 0;
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        String name = StringArgumentType.getString(context, "name");
        
        if (WaypointManager.removeWaypoint(player.getUuid(), name)) {
            if (!PlayerStateManager.isHideResponses(player.getUuid())) {
                source.sendFeedback(() -> Text.literal("Removed waypoint '")
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal(name).formatted(Formatting.AQUA))
                    .append(Text.literal("'").formatted(Formatting.YELLOW)), false);
            }
            return 1;
        } else {
            source.sendFeedback(() -> Text.literal("Waypoint '")
                .formatted(Formatting.RED)
                .append(Text.literal(name).formatted(Formatting.AQUA))
                .append(Text.literal("' not found").formatted(Formatting.RED)), false);
            return 0;
        }
    }
}




