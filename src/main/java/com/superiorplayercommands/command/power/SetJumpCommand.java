package com.superiorplayercommands.command.power;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SetJumpCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("setjump")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("value", FloatArgumentType.floatArg(0.1f, 50.0f))
                    .executes(SetJumpCommand::executeValue))
                .then(CommandManager.literal("reset")
                    .executes(SetJumpCommand::executeReset))
                .then(CommandManager.literal("rs")
                    .executes(SetJumpCommand::executeReset))
        );
    }
    
    private static int executeValue(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        float value = FloatArgumentType.getFloat(context, "value");
        
        PlayerStateManager.setJumpMultiplier(player.getUuid(), value);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Jump multiplier set to ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.format("%.1fx", value))
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
    
    private static int executeReset(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        PlayerStateManager.setJumpMultiplier(player.getUuid(), 1.0f);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Jump multiplier reset to default")
                .formatted(Formatting.YELLOW), false);
        }
        
        return 1;
    }
}
