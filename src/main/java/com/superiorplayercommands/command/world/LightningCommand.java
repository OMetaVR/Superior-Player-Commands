package com.superiorplayercommands.command.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LightningCommand {
    
    private static final double MAX_DISTANCE = 256.0;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("lightning")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> executeAtLook(context, 0))
                .then(CommandManager.argument("duration", LongArgumentType.longArg(0, 60000))
                    .executes(context -> executeAtLook(context, LongArgumentType.getLong(context, "duration")))
                    .then(CommandManager.argument("x", IntegerArgumentType.integer())
                        .then(CommandManager.argument("y", IntegerArgumentType.integer())
                            .then(CommandManager.argument("z", IntegerArgumentType.integer())
                                .executes(LightningCommand::executeAtCoords)
                            )
                        )
                    )
                )
        );
    }
    
    private static int executeAtLook(CommandContext<ServerCommandSource> context, long durationMs) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = player.getServerWorld();
        
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVector();
        Vec3d endPos = eyePos.add(lookVec.multiply(MAX_DISTANCE));
        
        BlockHitResult hitResult = world.raycast(new RaycastContext(
            eyePos, endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        ));
        
        if (hitResult.getType() == HitResult.Type.MISS) {
            source.sendFeedback(() -> Text.literal("No block in sight")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        BlockPos pos = hitResult.getBlockPos();
        spawnLightning(world, pos);
        
        if (durationMs > 0) {
            long strikes = durationMs / 500;
            for (int i = 1; i <= strikes; i++) {
                final int delay = i * 500;
                scheduler.schedule(() -> {
                    if (world.getServer() != null) {
                        world.getServer().execute(() -> spawnLightning(world, pos));
                    }
                }, delay, TimeUnit.MILLISECONDS);
            }
        }
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            if (durationMs > 0) {
                source.sendFeedback(() -> Text.literal("Lightning storm for " + durationMs + "ms!")
                    .formatted(Formatting.YELLOW), false);
            } else {
                source.sendFeedback(() -> Text.literal("⚡ Strike!")
                    .formatted(Formatting.YELLOW), false);
            }
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
        
        long durationMs = LongArgumentType.getLong(context, "duration");
        int x = IntegerArgumentType.getInteger(context, "x");
        int y = IntegerArgumentType.getInteger(context, "y");
        int z = IntegerArgumentType.getInteger(context, "z");
        
        BlockPos pos = new BlockPos(x, y, z);
        spawnLightning(world, pos);
        
        if (durationMs > 0) {
            long strikes = durationMs / 500;
            for (int i = 1; i <= strikes; i++) {
                final int delay = i * 500;
                scheduler.schedule(() -> {
                    if (world.getServer() != null) {
                        world.getServer().execute(() -> spawnLightning(world, pos));
                    }
                }, delay, TimeUnit.MILLISECONDS);
            }
        }
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("⚡ Strike at (" + x + ", " + y + ", " + z + ")!")
                .formatted(Formatting.YELLOW), false);
        }
        
        return 1;
    }
    
    private static void spawnLightning(ServerWorld world, BlockPos pos) {
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            world.spawnEntity(lightning);
        }
    }
}
