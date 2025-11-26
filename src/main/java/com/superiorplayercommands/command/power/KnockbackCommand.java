package com.superiorplayercommands.command.power;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class KnockbackCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("knockback")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(KnockbackCommand::showOrReset)
                .then(CommandManager.argument("multiplier", FloatArgumentType.floatArg(0.0f, 100.0f))
                    .executes(KnockbackCommand::setMultiplier)
                )
        );
    }
    
    private static int showOrReset(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        if (PlayerStateManager.hasKnockbackModifier(player.getUuid())) {
            PlayerStateManager.setKnockbackMultiplier(player.getUuid(), 1.0f);
            source.sendFeedback(() -> Text.literal("Knockback reset to ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("normal")
                    .formatted(Formatting.GRAY)), false);
        } else {
            source.sendFeedback(() -> Text.literal("Knockback is ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("normal")
                    .formatted(Formatting.WHITE))
                .append(Text.literal(" - Use /knockback <multiplier> to change")
                    .formatted(Formatting.GRAY)), false);
        }
        
        return 1;
    }
    
    private static int setMultiplier(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        float multiplier = FloatArgumentType.getFloat(context, "multiplier");
        
        PlayerStateManager.setKnockbackMultiplier(player.getUuid(), multiplier);
        
        if (multiplier == 0) {
            source.sendFeedback(() -> Text.literal("Knockback ")
                .formatted(Formatting.GREEN)
                .append(Text.literal("disabled")
                    .formatted(Formatting.RED))
                .append(Text.literal(" - Hits won't knock back entities")
                    .formatted(Formatting.GRAY)), false);
        } else if (multiplier == 1.0f) {
            source.sendFeedback(() -> Text.literal("Knockback reset to ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("normal")
                    .formatted(Formatting.GRAY)), false);
        } else {
            source.sendFeedback(() -> Text.literal("Knockback set to ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.format("%.1fx", multiplier))
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
}
