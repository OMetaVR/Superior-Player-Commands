package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RepairCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("repair")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(RepairCommand::executeHeld)
        );
        
        dispatcher.register(
            CommandManager.literal("repairall")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(RepairCommand::executeAll)
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
        
        if (!heldItem.isDamageable()) {
            source.sendFeedback(() -> Text.literal("This item cannot be repaired")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        if (heldItem.getDamage() == 0) {
            source.sendFeedback(() -> Text.literal("Item is already at full durability")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        heldItem.setDamage(0);
        
        source.sendFeedback(() -> Text.literal("Repaired ")
            .formatted(Formatting.GREEN)
            .append(Text.literal(heldItem.getName().getString())
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
        int repairedCount = 0;
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.isDamageable() && stack.getDamage() > 0) {
                stack.setDamage(0);
                repairedCount++;
            }
        }
        
        for (ItemStack armorStack : player.getArmorItems()) {
            if (!armorStack.isEmpty() && armorStack.isDamageable() && armorStack.getDamage() > 0) {
                armorStack.setDamage(0);
                repairedCount++;
            }
        }
        
        ItemStack offhand = player.getOffHandStack();
        if (!offhand.isEmpty() && offhand.isDamageable() && offhand.getDamage() > 0) {
            offhand.setDamage(0);
            repairedCount++;
        }
        
        if (repairedCount == 0) {
            source.sendFeedback(() -> Text.literal("No items needed repairing")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        final int count = repairedCount;
        source.sendFeedback(() -> Text.literal("Repaired ")
            .formatted(Formatting.GREEN)
            .append(Text.literal(String.valueOf(count))
                .formatted(Formatting.AQUA))
            .append(Text.literal(" item" + (count != 1 ? "s" : ""))
                .formatted(Formatting.GREEN)), false);
        
        return repairedCount;
    }
}



