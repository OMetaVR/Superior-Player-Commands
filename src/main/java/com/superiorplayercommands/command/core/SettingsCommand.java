package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.superiorplayercommands.client.ModKeybindings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SettingsCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("spcsettings")
            .executes(context -> {
                // Schedule opening the screen on the client thread
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null) {
                    client.execute(() -> ModKeybindings.openSettingsScreen(client));
                }
                return 1;
            })
        );
    }
}
