package com.superiorplayercommands.command.power;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullbrightCommand {
    
    private static final int INFINITE_DURATION = Integer.MAX_VALUE;
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("fullbright")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(FullbrightCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        if (player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            source.sendFeedback(() -> Text.literal("Fullbright ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("disabled")
                    .formatted(Formatting.GRAY)), false);
        } else {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                INFINITE_DURATION,
                0,
                true,
                false,
                false
            ));
            source.sendFeedback(() -> Text.literal("Fullbright ")
                .formatted(Formatting.GREEN)
                .append(Text.literal("enabled")
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
}
