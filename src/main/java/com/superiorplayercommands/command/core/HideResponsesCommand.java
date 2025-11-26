package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HideResponsesCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("hideresponses")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(HideResponsesCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        boolean newState = PlayerStateManager.toggleHideResponses(player.getUuid());
        
        if (newState) {
            source.sendFeedback(() -> Text.literal("Command responses ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("hidden")
                    .formatted(Formatting.GRAY)), false);
        } else {
            source.sendFeedback(() -> Text.literal("Command responses ")
                .formatted(Formatting.GREEN)
                .append(Text.literal("visible")
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
}
