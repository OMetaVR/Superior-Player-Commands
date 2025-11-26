package com.superiorplayercommands.command.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class BiomeCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("biome")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(BiomeCommand::execute)
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
        
        RegistryEntry<Biome> biomeEntry = player.getWorld().getBiome(pos);
        
        String biomeName = biomeEntry.getKey()
            .map(key -> key.getValue().getPath())
            .orElse("unknown");
        
        String formattedName = formatBiomeName(biomeName);
        
        source.sendFeedback(() -> Text.literal("Current biome: ")
            .formatted(Formatting.GRAY)
            .append(Text.literal(formattedName)
                .formatted(Formatting.GREEN)), false);
        
        source.sendFeedback(() -> Text.literal("ID: ")
            .formatted(Formatting.DARK_GRAY)
            .append(Text.literal("minecraft:" + biomeName)
                .formatted(Formatting.GRAY)), false);
        
        return 1;
    }
    
    private static String formatBiomeName(String name) {
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            }
        }
        
        return result.toString();
    }
}
