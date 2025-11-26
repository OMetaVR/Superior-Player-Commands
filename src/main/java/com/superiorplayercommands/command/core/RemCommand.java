package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.superiorplayercommands.data.WaypointManager;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
                // /rem - list all waypoints
                .executes(RemCommand::listWaypoints)
                // /rem <name> - remove a waypoint
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .suggests(WAYPOINT_SUGGESTIONS)
                    .executes(RemCommand::execute)
                )
        );
    }
    
    private static int listWaypoints(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        Map<String, WaypointManager.WaypointData> waypoints = WaypointManager.getWaypoints(player.getUuid());
        
        if (waypoints.isEmpty()) {
            source.sendFeedback(() -> Text.literal("You have no waypoints. Use ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("/set <name>").formatted(Formatting.YELLOW))
                .append(Text.literal(" to create one.").formatted(Formatting.GRAY)), false);
            return 0;
        }
        
        source.sendFeedback(() -> Text.literal("=== Your Waypoints ===")
            .formatted(Formatting.GOLD), false);
        
        for (Map.Entry<String, WaypointManager.WaypointData> entry : waypoints.entrySet()) {
            String name = entry.getKey();
            WaypointManager.WaypointData data = entry.getValue();
            
            source.sendFeedback(() -> Text.literal("  " + name)
                .formatted(Formatting.AQUA)
                .append(Text.literal(" → ").formatted(Formatting.GRAY))
                .append(Text.literal(String.format("(%d, %d, %d)", data.x, data.y, data.z))
                    .formatted(Formatting.WHITE))
                .append(Text.literal(" [" + data.getDimensionId().getPath() + "]")
                    .formatted(Formatting.DARK_GRAY)), false);
        }
        
        return waypoints.size();
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
            source.sendFeedback(() -> Text.literal("Removed waypoint '")
                .formatted(Formatting.YELLOW)
                .append(Text.literal(name).formatted(Formatting.AQUA))
                .append(Text.literal("'").formatted(Formatting.YELLOW)), false);
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



