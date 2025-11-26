package com.superiorplayercommands.command.power;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DropsCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("drops")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(DropsCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        boolean newState = PlayerStateManager.toggleDrops(player.getUuid());
        
        if (newState) {
            source.sendFeedback(() -> Text.literal("Block drops ")
                .formatted(Formatting.GREEN)
                .append(Text.literal("enabled")
                    .formatted(Formatting.AQUA)), false);
        } else {
            source.sendFeedback(() -> Text.literal("Block drops ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("disabled")
                    .formatted(Formatting.RED))
                .append(Text.literal(" - blocks will not drop items")
                    .formatted(Formatting.GRAY)), false);
        }
        
        return 1;
    }
}




