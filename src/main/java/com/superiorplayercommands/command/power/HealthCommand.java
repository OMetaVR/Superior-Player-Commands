package com.superiorplayercommands.command.power;

import com.mojang.brigadier.CommandDispatcher;
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

public class HealthCommand {
    
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("health")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("min")
                    .executes(context -> executeHealth(context, "min")))
                .then(CommandManager.literal("max")
                    .executes(context -> executeHealth(context, "max")))
                .then(CommandManager.literal("infinite")
                    .executes(context -> executeHealth(context, "infinite")))
                .then(CommandManager.argument("hearts", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 500))
                    .executes(HealthCommand::executeHearts))
        );
    }
    
    private static int executeHearts(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        int hearts = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "hearts");
        float health = hearts * 2.0f; // 1 heart = 2 health points
        
        EntityAttributeInstance healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr == null) return 0;
        
        healthAttr.removeModifier(HEALTH_MODIFIER_UUID);
        
        if (health != 20.0f) {
            double currentMax = healthAttr.getBaseValue();
            healthAttr.addPersistentModifier(new EntityAttributeModifier(
                HEALTH_MODIFIER_UUID, "Health modifier", health - currentMax,
                EntityAttributeModifier.Operation.ADDITION));
        }
        
        player.setHealth(health);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Health set to ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(hearts + " hearts")
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
    
    private static int executeHealth(CommandContext<ServerCommandSource> context, String mode) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        EntityAttributeInstance healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        
        if (healthAttr == null) return 0;
        
        healthAttr.removeModifier(HEALTH_MODIFIER_UUID);
        
        switch (mode) {
            case "min" -> {
                double currentMax = healthAttr.getBaseValue();
                healthAttr.addPersistentModifier(new EntityAttributeModifier(
                    HEALTH_MODIFIER_UUID, "Health modifier", 2.0 - currentMax, 
                    EntityAttributeModifier.Operation.ADDITION));
                player.setHealth(2.0f);
                
                if (!PlayerStateManager.isHideResponses(player.getUuid())) {
                    source.sendFeedback(() -> Text.literal("Health set to ")
                        .formatted(Formatting.YELLOW)
                        .append(Text.literal("minimum (1 heart)")
                            .formatted(Formatting.RED)), false);
                }
            }
            case "max" -> {
                player.setHealth(20.0f);
                
                if (!PlayerStateManager.isHideResponses(player.getUuid())) {
                    source.sendFeedback(() -> Text.literal("Health set to ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal("maximum (10 hearts)")
                            .formatted(Formatting.AQUA)), false);
                }
            }
            case "infinite" -> {
                double currentMax = healthAttr.getBaseValue();
                healthAttr.addPersistentModifier(new EntityAttributeModifier(
                    HEALTH_MODIFIER_UUID, "Health modifier", 1000000.0 - currentMax,
                    EntityAttributeModifier.Operation.ADDITION));
                player.setHealth(1000000.0f);
                
                if (!PlayerStateManager.isHideResponses(player.getUuid())) {
                    source.sendFeedback(() -> Text.literal("Health set to ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal("infinite")
                            .formatted(Formatting.LIGHT_PURPLE)), false);
                }
            }
        }
        
        return 1;
    }
}
