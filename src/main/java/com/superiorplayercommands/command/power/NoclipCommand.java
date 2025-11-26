package com.superiorplayercommands.command.power;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.network.StateSync;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NoclipCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("noclip")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(NoclipCommand::toggle)
                .then(CommandManager.argument("speed", FloatArgumentType.floatArg(0.1f, 10.0f))
                    .executes(NoclipCommand::setSpeed)
                )
        );
    }
    
    private static int toggle(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        boolean newState = PlayerStateManager.toggleNoclip(player.getUuid());
        
        StateSync.sendStateUpdate(player, StateSync.STATE_NOCLIP, newState, 1.0f);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            if (newState) {
                source.sendFeedback(() -> Text.literal("Noclip ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal("enabled")
                        .formatted(Formatting.AQUA))
                    .append(Text.literal(" - God mode and mob ignore active")
                        .formatted(Formatting.GRAY)), false);
            } else {
                source.sendFeedback(() -> Text.literal("Noclip ")
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal("disabled")
                        .formatted(Formatting.GRAY)), false);
            }
        }
        
        return 1;
    }
    
    private static int setSpeed(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        float speed = FloatArgumentType.getFloat(context, "speed");
        
        if (!PlayerStateManager.isNoclipEnabled(player.getUuid())) {
            PlayerStateManager.toggleNoclip(player.getUuid());
        }
        
        StateSync.sendStateUpdate(player, StateSync.STATE_NOCLIP, true, speed);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Noclip speed set to ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.format("%.1f", speed))
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
}
