package com.superiorplayercommands.command.power;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.function.Predicate;

public class RideCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("ride")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(RideCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        if (player.hasVehicle()) {
            player.stopRiding();
            if (!PlayerStateManager.isHideResponses(player.getUuid())) {
                source.sendFeedback(() -> Text.literal("Dismounted")
                    .formatted(Formatting.YELLOW), false);
            }
            return 1;
        }
        
        Entity target = raycastEntity(player, 64.0);
        
        if (target == null) {
            source.sendFeedback(() -> Text.literal("No entity in sight")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        player.startRiding(target, true);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            String entityName = target.getName().getString();
            source.sendFeedback(() -> Text.literal("Now riding ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(entityName)
                    .formatted(Formatting.AQUA)), false);
        }
        
        return 1;
    }
    
    private static Entity raycastEntity(ServerPlayerEntity player, double maxDistance) {
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVector();
        Vec3d end = start.add(direction.multiply(maxDistance));
        
        Box searchBox = player.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(1.0);
        
        Entity closest = null;
        double closestDistance = maxDistance;
        
        for (Entity entity : player.getWorld().getOtherEntities(player, searchBox, e -> !e.isSpectator() && e.canHit())) {
            Box entityBox = entity.getBoundingBox().expand(entity.getTargetingMargin());
            Optional<Vec3d> hit = entityBox.raycast(start, end);
            
            if (hit.isPresent()) {
                double distance = start.distanceTo(hit.get());
                if (distance < closestDistance) {
                    closest = entity;
                    closestDistance = distance;
                }
            }
        }
        
        return closest;
    }
}
