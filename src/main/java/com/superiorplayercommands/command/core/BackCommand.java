package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.data.PlayerStateManager.DeathPosition;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BackCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("back")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(BackCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        DeathPosition deathPos = PlayerStateManager.getLastDeathPosition(player.getUuid());
        
        if (deathPos == null) {
            source.sendFeedback(() -> Text.literal("No death location recorded")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        BlockPos targetPos = deathPos.pos;
        RegistryKey<World> targetDimension = RegistryKey.of(RegistryKeys.WORLD, deathPos.dimension);
        ServerWorld targetWorld = source.getServer().getWorld(targetDimension);
        
        if (targetWorld == null) {
            source.sendFeedback(() -> Text.literal("Cannot teleport: dimension not found")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        player.teleport(targetWorld,
            targetPos.getX() + 0.5,
            targetPos.getY(),
            targetPos.getZ() + 0.5,
            player.getYaw(),
            player.getPitch());
        
        source.sendFeedback(() -> Text.literal("Returned to death location")
            .formatted(Formatting.GREEN), false);
        
        return 1;
    }
}
