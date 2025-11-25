package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DestroyCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("destroy")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(DestroyCommand::executeHeld)
        );
        
        dispatcher.register(
            CommandManager.literal("destroyall")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(DestroyCommand::executeAll)
        );
        
        dispatcher.register(
            CommandManager.literal("clearinv")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(DestroyCommand::executeClearInv)
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
        
        String itemName = heldItem.getName().getString();
        int count = heldItem.getCount();
        
        player.getInventory().setStack(player.getInventory().selectedSlot, ItemStack.EMPTY);
        
        source.sendFeedback(() -> Text.literal("Destroyed ")
            .formatted(Formatting.YELLOW)
            .append(Text.literal(count + "x ")
                .formatted(Formatting.WHITE))
            .append(Text.literal(itemName)
                .formatted(Formatting.AQUA)), false);
        
        return 1;
    }
    
    private static int executeAll(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        int brokenCount = 0;
        
        // Break all damageable items in main inventory
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.isDamageable()) {
                stack.setDamage(stack.getMaxDamage()); // Set to max damage (broken)
                brokenCount++;
            }
        }
        
        // Break armor
        for (ItemStack armorStack : player.getArmorItems()) {
            if (!armorStack.isEmpty() && armorStack.isDamageable()) {
                armorStack.setDamage(armorStack.getMaxDamage());
                brokenCount++;
            }
        }
        
        // Break offhand
        ItemStack offhand = player.getOffHandStack();
        if (!offhand.isEmpty() && offhand.isDamageable()) {
            offhand.setDamage(offhand.getMaxDamage());
            brokenCount++;
        }
        
        if (brokenCount == 0) {
            source.sendFeedback(() -> Text.literal("No damageable items found")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        final int count = brokenCount;
        source.sendFeedback(() -> Text.literal("Broke ")
            .formatted(Formatting.YELLOW)
            .append(Text.literal(String.valueOf(count))
                .formatted(Formatting.RED))
            .append(Text.literal(" item" + (count != 1 ? "s" : ""))
                .formatted(Formatting.YELLOW)), false);
        
        return brokenCount;
    }
    
    private static int executeClearInv(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        int clearedCount = 0;
        
        // Count items before clearing
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (!player.getInventory().getStack(i).isEmpty()) {
                clearedCount++;
            }
        }
        
        // Check armor
        for (ItemStack armorStack : player.getArmorItems()) {
            if (!armorStack.isEmpty()) {
                clearedCount++;
            }
        }
        
        // Check offhand
        if (!player.getOffHandStack().isEmpty()) {
            clearedCount++;
        }
        
        if (clearedCount == 0) {
            source.sendFeedback(() -> Text.literal("Inventory is already empty")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        // Clear everything
        player.getInventory().clear();
        
        final int count = clearedCount;
        source.sendFeedback(() -> Text.literal("Cleared ")
            .formatted(Formatting.YELLOW)
            .append(Text.literal(String.valueOf(count))
                .formatted(Formatting.WHITE))
            .append(Text.literal(" item stack" + (count != 1 ? "s" : ""))
                .formatted(Formatting.YELLOW)), false);
        
        return clearedCount;
    }
}

