package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.WaypointManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class SetCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("set")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(SetCommand::execute)
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
        BlockPos pos = player.getBlockPos();
        
        boolean exists = WaypointManager.getWaypoint(player.getUuid(), name).isPresent();
        
        WaypointManager.setWaypoint(player.getUuid(), name, pos, player.getWorld().getRegistryKey());
        
        String action = exists ? "Updated" : "Created";
        source.sendFeedback(() -> Text.literal(action + " waypoint '")
            .formatted(Formatting.GREEN)
            .append(Text.literal(name).formatted(Formatting.AQUA))
            .append(Text.literal("' at ").formatted(Formatting.GREEN))
            .append(Text.literal(String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ()))
                .formatted(Formatting.WHITE)), false);
        
        return 1;
    }
}




