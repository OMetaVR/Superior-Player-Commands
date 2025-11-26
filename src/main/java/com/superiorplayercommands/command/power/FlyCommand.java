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

public class FlyCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("fly")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(FlyCommand::toggle)
                .then(CommandManager.argument("speed", FloatArgumentType.floatArg(0.0f, 10.0f))
                    .executes(FlyCommand::setSpeed)
                )
        );
    }
    
    private static int toggle(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        boolean newState = PlayerStateManager.toggleFly(player.getUuid());
        
        player.getAbilities().allowFlying = newState || player.isCreative() || player.isSpectator();
        if (!newState && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().flying = false;
        }
        player.sendAbilitiesUpdate();
        
        if (newState) {
            float speed = PlayerStateManager.getFlySpeed(player.getUuid());
            source.sendFeedback(() -> Text.literal("Flight ")
                .formatted(Formatting.GREEN)
                .append(Text.literal("enabled")
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" (speed: " + String.format("%.2f", speed) + ")")
                    .formatted(Formatting.GRAY)), false);
        } else {
            source.sendFeedback(() -> Text.literal("Flight ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("disabled")
                    .formatted(Formatting.GRAY)), false);
        }
        
        return 1;
    }
    
    private static int setSpeed(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        float speed = FloatArgumentType.getFloat(context, "speed");
        
        PlayerStateManager.setFlySpeed(player.getUuid(), speed * 0.05f);
        player.getAbilities().setFlySpeed(speed * 0.05f);
        player.sendAbilitiesUpdate();
        
        if (!PlayerStateManager.isFlyEnabled(player.getUuid())) {
            PlayerStateManager.setFlyEnabled(player.getUuid(), true);
            player.getAbilities().allowFlying = true;
            player.sendAbilitiesUpdate();
        }
        
        source.sendFeedback(() -> Text.literal("Flight speed set to ")
            .formatted(Formatting.GREEN)
            .append(Text.literal(String.format("%.1f", speed))
                .formatted(Formatting.AQUA)), false);
        
        return 1;
    }
}
