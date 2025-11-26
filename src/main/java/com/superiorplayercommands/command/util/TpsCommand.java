package com.superiorplayercommands.command.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TpsCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("tps")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(TpsCommand::execute)
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getServer();
        
        double mspt = getAverageTickTime(server);
        double tps = Math.min(20.0, 1000.0 / mspt);
        
        Formatting tpsColor;
        if (tps >= 19.5) {
            tpsColor = Formatting.GREEN;
        } else if (tps >= 15.0) {
            tpsColor = Formatting.YELLOW;
        } else if (tps >= 10.0) {
            tpsColor = Formatting.GOLD;
        } else {
            tpsColor = Formatting.RED;
        }
        
        Formatting msptColor;
        if (mspt <= 50) {
            msptColor = Formatting.GREEN;
        } else if (mspt <= 100) {
            msptColor = Formatting.YELLOW;
        } else {
            msptColor = Formatting.RED;
        }
        
        source.sendFeedback(() -> Text.literal("TPS: ")
            .formatted(Formatting.GRAY)
            .append(Text.literal(String.format("%.1f", tps))
                .formatted(tpsColor))
            .append(Text.literal(" / 20")
                .formatted(Formatting.DARK_GRAY)), false);
        
        source.sendFeedback(() -> Text.literal("MSPT: ")
            .formatted(Formatting.GRAY)
            .append(Text.literal(String.format("%.2f", mspt))
                .formatted(msptColor))
            .append(Text.literal("ms")
                .formatted(Formatting.DARK_GRAY)), false);
        
        return 1;
    }
    
    private static double getAverageTickTime(MinecraftServer server) {
        long[] tickTimes = server.lastTickLengths;
        if (tickTimes == null || tickTimes.length == 0) {
            return 50.0;
        }
        
        long sum = 0;
        for (long tickTime : tickTimes) {
            sum += tickTime;
        }
        
        return (sum / (double) tickTimes.length) / 1_000_000.0;
    }
}
