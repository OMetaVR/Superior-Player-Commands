package com.superiorplayercommands.command.power;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class SetSpeedCommand {
    
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("setspeed")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("value", FloatArgumentType.floatArg(0.1f, 20.0f))
                    .executes(SetSpeedCommand::executeValue))
                .then(CommandManager.literal("reset")
                    .executes(SetSpeedCommand::executeReset))
                .then(CommandManager.literal("rs")
                    .executes(SetSpeedCommand::executeReset))
        );
    }
    
    private static int executeValue(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        float value = FloatArgumentType.getFloat(context, "value");
        
        PlayerStateManager.setSpeedMultiplier(player.getUuid(), value);
        
        applySpeedModifier(player, value);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Speed multiplier set to ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.format("%.1fx", value))
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
    
    private static int executeReset(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        PlayerStateManager.setSpeedMultiplier(player.getUuid(), 1.0f);
        
        removeSpeedModifier(player);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Speed multiplier reset to default")
                .formatted(Formatting.YELLOW), false);
        }
        
        return 1;
    }
    
    private static void applySpeedModifier(ServerPlayerEntity player, float multiplier) {
        EntityAttributeInstance speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_MODIFIER_UUID);
            if (multiplier != 1.0f) {
                // Base speed is 0.1, so add (multiplier - 1) * base
                double bonus = (multiplier - 1.0) * 0.1;
                speedAttr.addTemporaryModifier(new EntityAttributeModifier(
                    SPEED_MODIFIER_UUID, "Speed modifier", bonus,
                    EntityAttributeModifier.Operation.ADDITION));
            }
        }
    }
    
    private static void removeSpeedModifier(ServerPlayerEntity player) {
        EntityAttributeInstance speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_MODIFIER_UUID);
        }
    }
}
