package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UsePortalCommand {

    private static final int NETHER_SCALE = 8;
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("useportal")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(UsePortalCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        RegistryKey<World> currentDimension = player.getWorld().getRegistryKey();
        
        ServerWorld targetWorld;
        double targetX, targetY, targetZ;
        String targetName;
        
        if (currentDimension == World.OVERWORLD) {
            targetWorld = source.getServer().getWorld(World.NETHER);
            targetX = player.getX() / NETHER_SCALE;
            targetY = Math.min(Math.max(player.getY(), 4), 120);
            targetZ = player.getZ() / NETHER_SCALE;
            targetName = "the Nether";
        } else if (currentDimension == World.NETHER) {
            targetWorld = source.getServer().getWorld(World.OVERWORLD);
            targetX = player.getX() * NETHER_SCALE;
            targetY = player.getY();
            targetZ = player.getZ() * NETHER_SCALE;
            targetName = "the Overworld";
        } else {
            targetWorld = source.getServer().getWorld(World.OVERWORLD);
            targetX = player.getX();
            targetY = player.getY();
            targetZ = player.getZ();
            targetName = "the Overworld";
        }
        
        if (targetWorld == null) {
            source.sendFeedback(() -> Text.literal("Target dimension not available")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        BlockPos targetPos = new BlockPos((int) targetX, (int) targetY, (int) targetZ);
        targetY = findSafeY(targetWorld, targetPos);
        
        player.teleport(targetWorld, targetX, targetY, targetZ, player.getYaw(), player.getPitch());
        
        final String finalTargetName = targetName;
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Teleported to " + finalTargetName)
                .formatted(Formatting.LIGHT_PURPLE), false);
        }
        
        return 1;
    }
    
    private static double findSafeY(ServerWorld world, BlockPos pos) {
        int y = pos.getY();
        
        for (int i = 0; i < 20; i++) {
            BlockPos check = new BlockPos(pos.getX(), y + i, pos.getZ());
            if (isSafe(world, check)) {
                return y + i;
            }
        }
        
        for (int i = 1; i < 20; i++) {
            BlockPos check = new BlockPos(pos.getX(), y - i, pos.getZ());
            if (isSafe(world, check)) {
                return y - i;
            }
        }
        
        return y;
    }
    
    private static boolean isSafe(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos).isAir() && 
               world.getBlockState(pos.up()).isAir() &&
               !world.getBlockState(pos.down()).isAir();
    }
}
