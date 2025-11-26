package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HealCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("heal")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(HealCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();
        
        if (currentHealth >= maxHealth) {
            source.sendFeedback(() -> Text.literal("Health is already full")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        player.setHealth(maxHealth);
        
        float healed = maxHealth - currentHealth;
        source.sendFeedback(() -> Text.literal("Restored ")
            .formatted(Formatting.GREEN)
            .append(Text.literal(String.format("%.1f", healed))
                .formatted(Formatting.RED))
            .append(Text.literal(" health")
                .formatted(Formatting.GREEN)), false);
        
        return 1;
    }
}



