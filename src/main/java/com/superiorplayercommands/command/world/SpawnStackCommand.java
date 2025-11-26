package com.superiorplayercommands.command.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpawnStackCommand {
    
    private static final SuggestionProvider<ServerCommandSource> MOB_SUGGESTIONS = (context, builder) -> {
        return CommandSource.suggestMatching(
            List.of("zombie", "skeleton", "creeper", "spider", "pig", "cow", "sheep", 
                "chicken", "villager", "enderman", "blaze", "witch", "slime"),
            builder
        );
    };
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("spawnstack")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("mobs", StringArgumentType.greedyString())
                    .suggests(MOB_SUGGESTIONS)
                    .executes(SpawnStackCommand::execute))
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        String input = StringArgumentType.getString(context, "mobs");
        String[] parts = input.split("\\s+");
        
        if (parts.length < 2) {
            source.sendFeedback(() -> Text.literal("Usage: /spawnstack <mob1> [mob2] [mob3] ... <repeat>")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        int repeatCount;
        try {
            repeatCount = Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            source.sendFeedback(() -> Text.literal("Last argument must be a number (repeat count)")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        if (repeatCount < 1 || repeatCount > 50) {
            source.sendFeedback(() -> Text.literal("Repeat count must be between 1 and 50")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        List<EntityType<?>> mobTypes = new ArrayList<>();
        for (int i = 0; i < parts.length - 1; i++) {
            String mobName = parts[i].toLowerCase();
            Identifier entityId = new Identifier("minecraft", mobName);
            Optional<EntityType<?>> entityType = Registries.ENTITY_TYPE.getOrEmpty(entityId);
            
            if (entityType.isEmpty()) {
                final String finalMobName = mobName;
                source.sendFeedback(() -> Text.literal("Unknown mob type: " + finalMobName)
                    .formatted(Formatting.RED), false);
                return 0;
            }
            mobTypes.add(entityType.get());
        }
        
        if (mobTypes.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No valid mob types specified")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        ServerWorld world = player.getServerWorld();
        BlockPos spawnPos = player.getBlockPos().offset(player.getHorizontalFacing(), 2);
        
        Entity bottomEntity = null;
        Entity previousEntity = null;
        int totalSpawned = 0;
        
        for (int repeat = 0; repeat < repeatCount; repeat++) {
            for (EntityType<?> mobType : mobTypes) {
                Entity entity = mobType.create(world, null, null, spawnPos, SpawnReason.COMMAND, false, false);
                if (entity != null) {
                    world.spawnEntity(entity);
                    
                    if (bottomEntity == null) {
                        bottomEntity = entity;
                    }
                    
                    if (previousEntity != null) {
                        entity.startRiding(previousEntity, true);
                    }
                    
                    previousEntity = entity;
                    totalSpawned++;
                }
            }
        }
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            final int count = totalSpawned;
            source.sendFeedback(() -> Text.literal("Spawned stack of ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(count))
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" mobs")
                    .formatted(Formatting.GREEN)), false);
        }
        
        return totalSpawned;
    }
}
