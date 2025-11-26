package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DuplicateCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("duplicate")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(DuplicateCommand::execute)
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
        
        ItemStack duplicate = heldItem.copy();
        
        if (!player.getInventory().insertStack(duplicate)) {
            ItemEntity itemEntity = player.dropItem(duplicate, false);
            if (itemEntity != null) {
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
            }
            source.sendFeedback(() -> Text.literal("Duplicated ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(heldItem.getCount() + "x ")
                    .formatted(Formatting.WHITE))
                .append(Text.literal(heldItem.getName().getString())
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" (dropped - inventory full)")
                    .formatted(Formatting.YELLOW)), false);
        } else {
            source.sendFeedback(() -> Text.literal("Duplicated ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(heldItem.getCount() + "x ")
                    .formatted(Formatting.WHITE))
                .append(Text.literal(heldItem.getName().getString())
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
}



