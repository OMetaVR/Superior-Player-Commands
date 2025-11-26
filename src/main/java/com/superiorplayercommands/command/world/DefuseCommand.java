package com.superiorplayercommands.command.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;

import java.util.List;

public class DefuseCommand {
    
    private static final int DEFAULT_RADIUS = 15;
    private static final int MAX_RADIUS = 256;
    private static final int ALL_RADIUS = 512; // "all" uses render distance approximation, idk how well this works tho
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("defuse")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> execute(context, DEFAULT_RADIUS))
                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                    .executes(context -> execute(context, IntegerArgumentType.getInteger(context, "radius")))
                )
                .then(CommandManager.literal("all")
                    .executes(context -> execute(context, ALL_RADIUS))
                )
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context, int radius) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        Box area = player.getBoundingBox().expand(radius);
        
        List<TntEntity> tntEntities = player.getWorld().getEntitiesByClass(
            TntEntity.class, area, tnt -> true
        );
        
        int defused = 0;
        for (TntEntity tnt : tntEntities) {
            tnt.discard();
            defused++;
        }
        
        final int count = defused;
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            if (count > 0) {
                source.sendFeedback(() -> Text.literal("Defused ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(String.valueOf(count))
                        .formatted(Formatting.AQUA))
                    .append(Text.literal(" TNT")
                        .formatted(Formatting.GREEN)), false);
            } else {
                source.sendFeedback(() -> Text.literal("No lit TNT found nearby")
                    .formatted(Formatting.GRAY), false);
            }
        }
        
        return count;
    }
}
