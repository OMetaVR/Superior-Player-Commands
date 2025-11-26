package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.util.ChestHelper;
import com.superiorplayercommands.util.ChestHelper.ChestResult;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class DropStoreCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("dropstore")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(DropStoreCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        List<ItemStack> itemsToStore = new ArrayList<>();
        List<Integer> mainSlots = new ArrayList<>();
        List<Integer> armorSlots = new ArrayList<>();
        boolean hasOffhand = false;
        
        for (int i = 0; i < player.getInventory().main.size(); i++) {
            ItemStack stack = player.getInventory().main.get(i);
            if (!stack.isEmpty()) {
                itemsToStore.add(stack.copy());
                mainSlots.add(i);
            }
        }
        
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (!stack.isEmpty()) {
                itemsToStore.add(stack.copy());
                armorSlots.add(i);
            }
        }
        
        ItemStack offhand = player.getInventory().offHand.get(0);
        if (!offhand.isEmpty()) {
            itemsToStore.add(offhand.copy());
            hasOffhand = true;
        }
        
        if (itemsToStore.isEmpty()) {
            source.sendFeedback(() -> Text.literal("Inventory is empty")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
        
        int stackCount = itemsToStore.size();
        ChestResult chest = ChestHelper.findOrCreateChest(player, stackCount);
        
        if (chest == null) {
            source.sendFeedback(() -> Text.literal("No valid position found for chest")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        int chestSlot = 0;
        for (ItemStack item : itemsToStore) {
            if (chestSlot < chest.inventory.size()) {
                chest.inventory.setStack(chestSlot, item);
                chestSlot++;
            }
        }
        
        for (int slot : mainSlots) {
            player.getInventory().main.set(slot, ItemStack.EMPTY);
        }
        for (int slot : armorSlots) {
            player.getInventory().armor.set(slot, ItemStack.EMPTY);
        }
        if (hasOffhand) {
            player.getInventory().offHand.set(0, ItemStack.EMPTY);
        }
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            final int count = itemsToStore.size();
            final boolean isDouble = chest.isDouble;
            source.sendFeedback(() -> Text.literal("Stored ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(count))
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" items in " + (isDouble ? "double " : "") + "chest at ")
                    .formatted(Formatting.GREEN))
                .append(Text.literal("(" + chest.primaryPos.getX() + ", " + chest.primaryPos.getY() + ", " + chest.primaryPos.getZ() + ")")
                    .formatted(Formatting.WHITE)), false);
        }
        
        return itemsToStore.size();
    }
}
