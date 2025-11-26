package com.superiorplayercommands.command.power;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.network.StateSync;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MobsIgnoreCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("mobsignore")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(MobsIgnoreCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        boolean newState = PlayerStateManager.toggleMobsIgnore(player.getUuid());
        
        StateSync.sendStateUpdate(player, StateSync.STATE_MOBS_IGNORE, newState);
        
        if (newState) {
            player.getWorld().getEntitiesByClass(
                net.minecraft.entity.mob.MobEntity.class,
                player.getBoundingBox().expand(64),
                mob -> mob.getTarget() == player
            ).forEach(mob -> mob.setTarget(null));
        }
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            if (newState) {
                source.sendFeedback(() -> Text.literal("Mobs ignore ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal("enabled")
                        .formatted(Formatting.AQUA))
                    .append(Text.literal(" - Hostile mobs will not target you")
                        .formatted(Formatting.GRAY)), false);
            } else {
                source.sendFeedback(() -> Text.literal("Mobs ignore ")
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal("disabled")
                        .formatted(Formatting.GRAY)), false);
            }
        }
        
        return 1;
    }
}
