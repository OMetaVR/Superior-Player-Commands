package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.data.PlayerStateManager;
import com.superiorplayercommands.util.TeleportHelper;
import com.superiorplayercommands.util.TeleportHelper.LiquidType;
import com.superiorplayercommands.util.TeleportHelper.ScanResult;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class AscendCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("ascend")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(AscendCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        BlockPos playerPos = player.getBlockPos();
        
        ScanResult result = TeleportHelper.scanUp(player.getWorld(), playerPos);
        
        if (!result.found) {
            source.sendFeedback(() -> Text.literal("No valid location found above you")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        if (result.liquidFound != LiquidType.NONE) {
            String liquidName = result.liquidFound == LiquidType.LAVA ? "lava" : "water";
            source.sendFeedback(() -> Text.literal("Warning: Destination contains " + liquidName + "!")
                .formatted(Formatting.YELLOW), false);
        }
        
        TeleportHelper.teleportPlayer(player, result.position);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            int blocksAscended = result.position.getY() - playerPos.getY();
            source.sendFeedback(() -> Text.literal("Ascended " + blocksAscended + " blocks")
                .formatted(Formatting.GREEN), false);
        }
        
        return 1;
    }
}

