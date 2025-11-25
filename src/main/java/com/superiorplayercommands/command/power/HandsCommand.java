package com.superiorplayercommands.command.power;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.data.PlayerStateManager.ToolTier;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HandsCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Register each tier as a separate command
        registerTierCommand(dispatcher, "woodhands", ToolTier.WOOD);
        registerTierCommand(dispatcher, "stonehands", ToolTier.STONE);
        registerTierCommand(dispatcher, "ironhands", ToolTier.IRON);
        registerTierCommand(dispatcher, "goldhands", ToolTier.GOLD);
        registerTierCommand(dispatcher, "diamondhands", ToolTier.DIAMOND);
        registerTierCommand(dispatcher, "netheritehands", ToolTier.NETHERITE);
        
        // Also register /hands to show current level or disable
        dispatcher.register(
            CommandManager.literal("hands")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(HandsCommand::showOrDisable)
        );
    }
    
    private static void registerTierCommand(CommandDispatcher<ServerCommandSource> dispatcher, String name, ToolTier tier) {
        dispatcher.register(
            CommandManager.literal(name)
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> setHandsLevel(context, tier))
        );
    }
    
    private static int setHandsLevel(CommandContext<ServerCommandSource> context, ToolTier tier) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        ToolTier currentTier = PlayerStateManager.getHandsLevel(player.getUuid());
        
        // If same tier, disable
        if (currentTier == tier) {
            PlayerStateManager.setHandsLevel(player.getUuid(), ToolTier.NONE);
            source.sendFeedback(() -> Text.literal("Tool hands ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("disabled")
                    .formatted(Formatting.GRAY)), false);
            return 1;
        }
        
        PlayerStateManager.setHandsLevel(player.getUuid(), tier);
        
        Formatting tierColor = getTierColor(tier);
        source.sendFeedback(() -> Text.literal("Hands now mine as ")
            .formatted(Formatting.GREEN)
            .append(Text.literal(tier.name)
                .formatted(tierColor))
            .append(Text.literal(" tools")
                .formatted(Formatting.GREEN)), false);
        
        return 1;
    }
    
    private static int showOrDisable(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        ToolTier currentTier = PlayerStateManager.getHandsLevel(player.getUuid());
        
        if (currentTier == ToolTier.NONE) {
            source.sendFeedback(() -> Text.literal("Tool hands is currently ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("disabled")
                    .formatted(Formatting.RED))
                .append(Text.literal("\nUse /woodhands, /stonehands, /ironhands, /goldhands, /diamondhands, or /netheritehands")
                    .formatted(Formatting.GRAY)), false);
        } else {
            // Disable it
            PlayerStateManager.setHandsLevel(player.getUuid(), ToolTier.NONE);
            source.sendFeedback(() -> Text.literal("Tool hands ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("disabled")
                    .formatted(Formatting.GRAY)), false);
        }
        
        return 1;
    }
    
    private static Formatting getTierColor(ToolTier tier) {
        return switch (tier) {
            case WOOD -> Formatting.GOLD;
            case STONE -> Formatting.GRAY;
            case IRON -> Formatting.WHITE;
            case GOLD -> Formatting.YELLOW;
            case DIAMOND -> Formatting.AQUA;
            case NETHERITE -> Formatting.DARK_PURPLE;
            default -> Formatting.WHITE;
        };
    }
}

