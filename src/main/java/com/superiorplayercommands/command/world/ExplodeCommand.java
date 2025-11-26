package com.superiorplayercommands.command.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class ExplodeCommand {
    
    private static final float DEFAULT_SIZE = 4.0f;
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("explode")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> executeAtPlayer(context, DEFAULT_SIZE))
                .then(CommandManager.argument("size", FloatArgumentType.floatArg(0.1f, 1000f))
                    .executes(context -> executeAtPlayer(context, FloatArgumentType.getFloat(context, "size")))
                    .then(CommandManager.argument("x", IntegerArgumentType.integer())
                        .then(CommandManager.argument("y", IntegerArgumentType.integer())
                            .then(CommandManager.argument("z", IntegerArgumentType.integer())
                                .executes(ExplodeCommand::executeAtCoords)
                            )
                        )
                    )
                )
        );
    }
    
    private static int executeAtPlayer(CommandContext<ServerCommandSource> context, float size) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = player.getServerWorld();
        
        world.createExplosion(null, player.getX(), player.getY(), player.getZ(), size, 
            World.ExplosionSourceType.TNT);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Boom! (size: " + size + ")")
                .formatted(Formatting.RED), false);
        }
        
        return 1;
    }
    
    private static int executeAtCoords(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = player.getServerWorld();
        
        float size = FloatArgumentType.getFloat(context, "size");
        int x = IntegerArgumentType.getInteger(context, "x");
        int y = IntegerArgumentType.getInteger(context, "y");
        int z = IntegerArgumentType.getInteger(context, "z");
        
        world.createExplosion(null, x + 0.5, y + 0.5, z + 0.5, size, World.ExplosionSourceType.TNT);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Boom at (" + x + ", " + y + ", " + z + ")! (size: " + size + ")")
                .formatted(Formatting.RED), false);
        }
        
        return 1;
    }
}
