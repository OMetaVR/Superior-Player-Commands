package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
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
                .executes(MoreCommand::executeHeld)
                .then(CommandManager.literal("all")
                    .executes(MoreCommand::executeAll))
        );
    }
    
    private static int executeHeld(CommandContext<ServerCommandSource> context) {
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
            if (!PlayerStateManager.isHideResponses(player.getUuid())) {
                source.sendFeedback(() -> Text.literal("Stack is already at maximum")
                    .formatted(Formatting.GRAY), false);
            }
            return 0;
        }
        
        int added = maxStack - currentCount;
        heldItem.setCount(maxStack);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Added ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(added))
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" to stack (now " + maxStack + ")")
                    .formatted(Formatting.GREEN)), false);
        }
        
        return 1;
    }
    
    private static int executeAll(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        int filled = 0;
        int totalAdded = 0;
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                int maxStack = stack.getMaxCount();
                int currentCount = stack.getCount();
                
                if (currentCount < maxStack) {
                    int added = maxStack - currentCount;
                    stack.setCount(maxStack);
                    totalAdded += added;
                    filled++;
                }
            }
        }
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            if (filled > 0) {
                final int count = filled;
                final int total = totalAdded;
                source.sendFeedback(() -> Text.literal("Filled ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(String.valueOf(count))
                        .formatted(Formatting.AQUA))
                    .append(Text.literal(" stacks (+")
                        .formatted(Formatting.GREEN))
                    .append(Text.literal(String.valueOf(total))
                        .formatted(Formatting.WHITE))
                    .append(Text.literal(" items)")
                        .formatted(Formatting.GREEN)), false);
            } else {
                source.sendFeedback(() -> Text.literal("All stacks are already at maximum")
                    .formatted(Formatting.GRAY), false);
            }
        }
        
        return filled;
    }
}
