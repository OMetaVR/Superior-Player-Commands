package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DropCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("drop")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(DropCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        if (!source.getServer().isSingleplayer() && !PlayerStateManager.hasSeenDropWarning(player.getUuid())) {
            PlayerStateManager.setDropWarningShown(player.getUuid());
            source.sendFeedback(() -> Text.literal("⚠ Warning: ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("Using /drop on multiplayer servers with anti-cheat may flag your account. ")
                    .formatted(Formatting.GRAY))
                .append(Text.literal("[Run again to confirm]")
                    .formatted(Formatting.YELLOW)
                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/drop")))), false);
            return 0;
        }
        
        int droppedCount = 0;
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                dropItem(player, stack.copy());
                player.getInventory().setStack(i, ItemStack.EMPTY);
                droppedCount++;
            }
        }
        
        for (int i = 0; i < 4; i++) {
            ItemStack armor = player.getInventory().armor.get(i);
            if (!armor.isEmpty()) {
                dropItem(player, armor.copy());
                player.getInventory().armor.set(i, ItemStack.EMPTY);
                droppedCount++;
            }
        }
        
        ItemStack offhand = player.getOffHandStack();
        if (!offhand.isEmpty()) {
            dropItem(player, offhand.copy());
            player.getInventory().offHand.set(0, ItemStack.EMPTY);
            droppedCount++;
        }
        
        final int count = droppedCount;
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            if (count > 0) {
                source.sendFeedback(() -> Text.literal("Dropped ")
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal(String.valueOf(count))
                        .formatted(Formatting.AQUA))
                    .append(Text.literal(" item stacks")
                        .formatted(Formatting.YELLOW)), false);
            } else {
                source.sendFeedback(() -> Text.literal("Inventory is empty")
                    .formatted(Formatting.GRAY), false);
            }
        }
        
        return count;
    }
    
    private static void dropItem(ServerPlayerEntity player, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(
            player.getWorld(),
            player.getX(),
            player.getY() + 0.5,
            player.getZ(),
            stack
        );
        itemEntity.setPickupDelay(40);
        player.getWorld().spawnEntity(itemEntity);
    }
}
