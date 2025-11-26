package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HungerCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("hunger")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(HungerCommand::showHunger)
                .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 20))
                    .executes(HungerCommand::setHunger)
                )
        );
    }
    
    private static int showHunger(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        int hunger = player.getHungerManager().getFoodLevel();
        float saturation = player.getHungerManager().getSaturationLevel();
        
        source.sendFeedback(() -> Text.literal("Hunger: ")
            .formatted(Formatting.GRAY)
            .append(Text.literal(String.valueOf(hunger))
                .formatted(Formatting.GOLD))
            .append(Text.literal("/20, Saturation: ")
                .formatted(Formatting.GRAY))
            .append(Text.literal(String.format("%.1f", saturation))
                .formatted(Formatting.YELLOW)), false);
        
        return hunger;
    }
    
    private static int setHunger(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        int level = IntegerArgumentType.getInteger(context, "level");
        
        player.getHungerManager().setFoodLevel(level);
        
        source.sendFeedback(() -> Text.literal("Hunger set to ")
            .formatted(Formatting.GREEN)
            .append(Text.literal(String.valueOf(level))
                .formatted(Formatting.GOLD)), false);
        
        return 1;
    }
}
