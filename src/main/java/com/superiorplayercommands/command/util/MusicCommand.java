package com.superiorplayercommands.command.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.network.MusicControlPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MusicCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("music")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.literal("play")
                    .executes(context -> executeControl(context, "play"))
                    .then(CommandManager.argument("volume", FloatArgumentType.floatArg(0.0f, 1.0f))
                        .executes(context -> executeControlWithVolume(context, "play", 
                            FloatArgumentType.getFloat(context, "volume")))))
                .then(CommandManager.literal("pause")
                    .executes(context -> executeControl(context, "pause")))
                .then(CommandManager.literal("skip")
                    .executes(context -> executeControl(context, "skip")))
                .then(CommandManager.literal("back")
                    .executes(context -> executeControl(context, "back")))
                .then(CommandManager.argument("volume", FloatArgumentType.floatArg(0.0f, 1.0f))
                    .executes(context -> executeVolume(context, 
                        FloatArgumentType.getFloat(context, "volume"))))
        );
    }
    
    private static int executeControl(CommandContext<ServerCommandSource> context, String action) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        MusicControlPacket.send(player, action, -1.0f);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            String message = switch (action) {
                case "play" -> "Resuming music";
                case "pause" -> "Pausing music";
                case "skip" -> "Skipping to next track";
                case "back" -> "Going to previous track";
                default -> "Music control: " + action;
            };
            source.sendFeedback(() -> Text.literal(message)
                .formatted(Formatting.GREEN), false);
        }
        
        return 1;
    }
    
    private static int executeControlWithVolume(CommandContext<ServerCommandSource> context, String action, float volume) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        MusicControlPacket.send(player, action, volume);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Resuming music at ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.format("%.0f%%", volume * 100))
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" volume")
                    .formatted(Formatting.GREEN)), false);
        }
        
        return 1;
    }
    
    private static int executeVolume(CommandContext<ServerCommandSource> context, float volume) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        MusicControlPacket.send(player, "volume", volume);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Music volume set to ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.format("%.0f%%", volume * 100))
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
}
