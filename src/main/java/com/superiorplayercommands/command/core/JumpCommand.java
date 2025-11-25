package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.util.TeleportHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class JumpCommand {
    
    // Maximum distance for raycast (in blocks)
    private static final double MAX_DISTANCE = 256.0;
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("jump")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(JumpCommand::execute)
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
        
        // Raycast from player's eye position in the direction they're looking
        BlockHitResult hitResult = world.raycast(new RaycastContext(
            player.getEyePos(),
            player.getEyePos().add(player.getRotationVector().multiply(MAX_DISTANCE)),
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
        
        // Find a safe landing spot on top of the hit block
        BlockPos landingPos = findLandingSpot(world, hitPos, hitResult);
        
        if (landingPos == null) {
            source.sendFeedback(() -> Text.literal("No safe landing spot found")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        // Calculate distance before teleporting
        BlockPos startPos = player.getBlockPos();
        double distance = Math.sqrt(startPos.getSquaredDistance(landingPos));
        
        // Store previous position for /return (TODO: implement return system)
        
        TeleportHelper.teleportPlayer(player, landingPos);
        
        source.sendFeedback(() -> Text.literal("Jumped " + String.format("%.1f", distance) + " blocks")
            .formatted(Formatting.GREEN), false);
        
        return 1;
    }
    
    /**
     * Find a safe landing spot based on where the player is looking
     * Prioritizes landing on top of the block they're looking at
     */
    private static BlockPos findLandingSpot(World world, BlockPos hitPos, BlockHitResult hitResult) {
        // Try landing on top of the hit block first
        BlockPos onTop = hitPos.up();
        if (TeleportHelper.hasTwoBlockGap(world, onTop)) {
            return onTop;
        }
        
        // If the hit block itself is passable (like looking at the side of a block),
        // try using the position based on which side was hit
        BlockPos adjacent = hitPos.offset(hitResult.getSide());
        if (TeleportHelper.hasTwoBlockGap(world, adjacent) && TeleportHelper.hasSolidFloor(world, adjacent)) {
            return adjacent;
        }
        
        // Try finding a spot nearby (scan up from hit position)
        for (int y = 0; y <= 3; y++) {
            BlockPos checkPos = hitPos.up(y + 1);
            if (TeleportHelper.hasTwoBlockGap(world, checkPos) && TeleportHelper.hasSolidFloor(world, checkPos)) {
                return checkPos;
            }
        }
        
        return null;
    }
}

