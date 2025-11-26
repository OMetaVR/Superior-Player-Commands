package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ReplenishCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            dispatcher.register(
            CommandManager.literal("saturation")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ReplenishCommand::execute)
        );
        
        dispatcher.register(
            CommandManager.literal("replenish")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ReplenishCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        HungerManager hunger = player.getHungerManager();
        
        int currentFood = hunger.getFoodLevel();
        float currentSaturation = hunger.getSaturationLevel();
        
        if (currentFood >= 20 && currentSaturation >= 20.0f) {
            source.sendFeedback(() -> Text.literal("Hunger is already full")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        hunger.setFoodLevel(20);
        hunger.setSaturationLevel(20.0f);
        hunger.setExhaustion(0.0f);
        
        source.sendFeedback(() -> Text.literal("Hunger and saturation restored")
            .formatted(Formatting.GREEN), false);
        
        return 1;
    }
}



