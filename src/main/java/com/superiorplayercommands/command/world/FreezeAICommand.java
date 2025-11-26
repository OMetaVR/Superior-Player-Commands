package com.superiorplayercommands.command.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FreezeAICommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("freeze")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(FreezeAICommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        boolean newState = PlayerStateManager.toggleFreezeAI();
        
        int affected = 0;
        for (ServerWorld world : source.getServer().getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof MobEntity mob) {
                    mob.setAiDisabled(newState);
                    affected++;
                }
            }
        }
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            final int count = affected;
            if (newState) {
                source.sendFeedback(() -> Text.literal("Froze AI for ")
                    .formatted(Formatting.AQUA)
                    .append(Text.literal(String.valueOf(count))
                        .formatted(Formatting.WHITE))
                    .append(Text.literal(" mobs")
                        .formatted(Formatting.AQUA)), false);
            } else {
                source.sendFeedback(() -> Text.literal("Unfroze AI for ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(String.valueOf(count))
                        .formatted(Formatting.WHITE))
                    .append(Text.literal(" mobs")
                        .formatted(Formatting.GREEN)), false);
            }
        }
        
        return affected;
    }
}
