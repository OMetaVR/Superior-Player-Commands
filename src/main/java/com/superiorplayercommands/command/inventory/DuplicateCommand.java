package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.util.ChestHelper;
import com.superiorplayercommands.util.ChestHelper.ChestResult;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class DuplicateCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("duplicate")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(DuplicateCommand::executeHeld)
                .then(CommandManager.literal("all")
                    .executes(DuplicateCommand::executeAll)
                    .then(CommandManager.literal("store")
                        .executes(DuplicateCommand::executeAllStore)))
                .then(CommandManager.literal("store")
                    .executes(DuplicateCommand::executeHeldStore))
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
        
        ItemStack duplicate = heldItem.copy();
        
        if (!player.getInventory().insertStack(duplicate)) {
            dropItem(player, duplicate);
            if (!PlayerStateManager.isHideResponses(player.getUuid())) {
                source.sendFeedback(() -> Text.literal("Duplicated ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(heldItem.getCount() + "x ")
                        .formatted(Formatting.WHITE))
                    .append(Text.literal(heldItem.getName().getString())
                        .formatted(Formatting.AQUA))
                    .append(Text.literal(" (dropped - inventory full)")
                        .formatted(Formatting.YELLOW)), false);
            }
        } else {
            if (!PlayerStateManager.isHideResponses(player.getUuid())) {
                source.sendFeedback(() -> Text.literal("Duplicated ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(heldItem.getCount() + "x ")
                        .formatted(Formatting.WHITE))
                    .append(Text.literal(heldItem.getName().getString())
                        .formatted(Formatting.AQUA)), false);
            }
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
        int duplicated = 0;
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                ItemStack duplicate = stack.copy();
                dropItem(player, duplicate);
                duplicated++;
            }
        }
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            final int count = duplicated;
            source.sendFeedback(() -> Text.literal("Duplicated ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(count))
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" item stacks (dropped on ground)")
                    .formatted(Formatting.GREEN)), false);
        }
        
        return duplicated;
    }
    
    private static int executeHeldStore(CommandContext<ServerCommandSource> context) {
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
        
        ChestResult chest = ChestHelper.findOrCreateChest(player, 1);
        if (chest == null) {
            source.sendFeedback(() -> Text.literal("Could not create chest")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        ItemStack duplicate = heldItem.copy();
        List<ItemStack> items = new ArrayList<>();
        items.add(duplicate);
        ChestHelper.dropOverflow(player.getWorld(), chest.primaryPos, items, chest.inventory);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Duplicated and stored ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(heldItem.getName().getString())
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
    
    private static int executeAllStore(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        List<ItemStack> itemsToDuplicate = new ArrayList<>();
        
        for (int i = 0; i < player.getInventory().main.size(); i++) {
            ItemStack stack = player.getInventory().main.get(i);
            if (!stack.isEmpty()) {
                itemsToDuplicate.add(stack.copy());
            }
        }
        
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (!stack.isEmpty()) {
                itemsToDuplicate.add(stack.copy());
            }
        }
        
        ItemStack offhand = player.getInventory().offHand.get(0);
        if (!offhand.isEmpty()) {
            itemsToDuplicate.add(offhand.copy());
        }
        
        if (itemsToDuplicate.isEmpty()) {
            source.sendFeedback(() -> Text.literal("Inventory is empty")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        int stackCount = itemsToDuplicate.size();
        ChestResult chest = ChestHelper.findOrCreateChest(player, stackCount);
        
        if (chest == null) {
            source.sendFeedback(() -> Text.literal("Could not create chest")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        int stored = 0;
        int chestSlot = 0;
        for (ItemStack item : itemsToDuplicate) {
            if (chestSlot < chest.inventory.size()) {
                chest.inventory.setStack(chestSlot, item.copy());
                chestSlot++;
                stored++;
            } else {
                ItemEntity itemEntity = new ItemEntity(player.getWorld(),
                    chest.primaryPos.getX() + 0.5, chest.primaryPos.getY() + 1, chest.primaryPos.getZ() + 0.5, item.copy());
                player.getWorld().spawnEntity(itemEntity);
                stored++;
            }
        }
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            final int count = stored;
            final boolean isDouble = chest.isDouble;
            source.sendFeedback(() -> Text.literal("Duplicated and stored ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(count))
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" items in " + (isDouble ? "double " : "") + "chest")
                    .formatted(Formatting.GREEN)), false);
        }
        
        return stored;
    }
    
    private static void dropItem(ServerPlayerEntity player, ItemStack stack) {
        ItemEntity itemEntity = player.dropItem(stack, false);
        if (itemEntity != null) {
            itemEntity.resetPickupDelay();
            itemEntity.setOwner(player.getUuid());
        }
    }
}

