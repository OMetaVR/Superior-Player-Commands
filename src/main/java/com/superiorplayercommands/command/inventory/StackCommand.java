package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StackCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("stack")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(StackCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        int stacksMerged = 0;
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty() || stack.getCount() >= stack.getMaxCount()) {
                continue;
            }
            
            for (int j = i + 1; j < player.getInventory().size(); j++) {
                ItemStack other = player.getInventory().getStack(j);
                if (other.isEmpty()) continue;
                
                if (ItemStack.canCombine(stack, other)) {
                    int space = stack.getMaxCount() - stack.getCount();
                    int toTransfer = Math.min(space, other.getCount());
                    
                    if (toTransfer > 0) {
                        stack.increment(toTransfer);
                        other.decrement(toTransfer);
                        stacksMerged++;
                        
                        if (other.isEmpty()) {
                            player.getInventory().setStack(j, ItemStack.EMPTY);
                        }
                        
                        if (stack.getCount() >= stack.getMaxCount()) {
                            break;
                        }
                    }
                }
            }
        }
        
        final int merged = stacksMerged;
        if (merged > 0) {
            source.sendFeedback(() -> Text.literal("Merged ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(merged))
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" partial stack" + (merged != 1 ? "s" : ""))
                    .formatted(Formatting.GREEN)), false);
        } else {
            source.sendFeedback(() -> Text.literal("No partial stacks to merge")
                .formatted(Formatting.GRAY), false);
        }
        
        return merged;
    }
}
