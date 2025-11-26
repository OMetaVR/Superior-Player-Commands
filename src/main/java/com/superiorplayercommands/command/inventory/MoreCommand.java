package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MoreCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("more")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(MoreCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        ItemStack heldItem = player.getMainHandStack();
        
        if (heldItem.isEmpty()) {
            source.sendFeedback(() -> Text.literal("You're not holding anything")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        int maxStack = heldItem.getMaxCount();
        int currentCount = heldItem.getCount();
        
        if (currentCount >= maxStack) {
            source.sendFeedback(() -> Text.literal("Stack is already at maximum")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        int added = maxStack - currentCount;
        heldItem.setCount(maxStack);
        
        source.sendFeedback(() -> Text.literal("Added ")
            .formatted(Formatting.GREEN)
            .append(Text.literal(String.valueOf(added))
                .formatted(Formatting.AQUA))
            .append(Text.literal(" to stack (now " + maxStack + ")")
                .formatted(Formatting.GREEN)), false);
        
        return 1;
    }
}
