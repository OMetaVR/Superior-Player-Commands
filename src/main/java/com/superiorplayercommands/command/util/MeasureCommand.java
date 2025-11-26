package com.superiorplayercommands.command.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class MeasureCommand {
    
    private static final double MAX_DISTANCE = 256.0;
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("measure")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(MeasureCommand::execute)
        );
        
            dispatcher.register(
            CommandManager.literal("distance")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(MeasureCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        World world = player.getWorld();
        
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVector();
        Vec3d endPos = eyePos.add(lookVec.multiply(MAX_DISTANCE));
        
        BlockHitResult hitResult = world.raycast(new RaycastContext(
            eyePos,
            endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        ));
        
        if (hitResult.getType() == HitResult.Type.MISS) {
            source.sendFeedback(() -> Text.literal("No block in sight (max range: " + (int)MAX_DISTANCE + " blocks)")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        BlockPos hitPos = hitResult.getBlockPos();
        BlockPos playerPos = player.getBlockPos();
        
        double exactDistance = eyePos.distanceTo(hitResult.getPos());
        double blockDistance = Math.sqrt(playerPos.getSquaredDistance(hitPos));
        
        int dx = hitPos.getX() - playerPos.getX();
        int dy = hitPos.getY() - playerPos.getY();
        int dz = hitPos.getZ() - playerPos.getZ();
        
        source.sendFeedback(() -> Text.literal("Distance: ")
            .formatted(Formatting.GRAY)
            .append(Text.literal(String.format("%.2f", exactDistance))
                .formatted(Formatting.AQUA))
            .append(Text.literal(" blocks")
                .formatted(Formatting.GRAY)), false);
        
        source.sendFeedback(() -> Text.literal("Target: ")
            .formatted(Formatting.GRAY)
            .append(Text.literal(String.format("(%d, %d, %d)", hitPos.getX(), hitPos.getY(), hitPos.getZ()))
                .formatted(Formatting.WHITE)), false);
        
        source.sendFeedback(() -> Text.literal("Delta: ")
            .formatted(Formatting.GRAY)
            .append(Text.literal(String.format("X:%d Y:%d Z:%d", dx, dy, dz))
                .formatted(Formatting.YELLOW)), false);
        
        return 1;
    }
}
