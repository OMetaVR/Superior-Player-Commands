package com.superiorplayercommands.command.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class CoordsCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("coords")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(CoordsCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        BlockPos pos = player.getBlockPos();
        String dimension = player.getWorld().getRegistryKey().getValue().getPath();
        
        source.sendFeedback(() -> Text.literal("Position: ")
            .formatted(Formatting.GRAY)
            .append(Text.literal(String.format("X: %d", pos.getX()))
                .formatted(Formatting.RED))
            .append(Text.literal(" Y: ")
                .formatted(Formatting.GRAY))
            .append(Text.literal(String.valueOf(pos.getY()))
                .formatted(Formatting.GREEN))
            .append(Text.literal(" Z: ")
                .formatted(Formatting.GRAY))
            .append(Text.literal(String.valueOf(pos.getZ()))
                .formatted(Formatting.BLUE))
            .append(Text.literal(" [" + dimension + "]")
                .formatted(Formatting.DARK_GRAY)), false);
        
        float yaw = player.getYaw();
        String facing = getCardinalDirection(yaw);
        
        source.sendFeedback(() -> Text.literal("Facing: ")
            .formatted(Formatting.GRAY)
            .append(Text.literal(facing)
                .formatted(Formatting.YELLOW))
            .append(Text.literal(String.format(" (%.1f°)", yaw))
                .formatted(Formatting.DARK_GRAY)), false);
        
        return 1;
    }
    
    private static String getCardinalDirection(float yaw) {
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;
        
        if (yaw >= 315 || yaw < 45) return "South (+Z)";
        if (yaw >= 45 && yaw < 135) return "West (-X)";
        if (yaw >= 135 && yaw < 225) return "North (-Z)";
        return "East (+X)";
    }
}
