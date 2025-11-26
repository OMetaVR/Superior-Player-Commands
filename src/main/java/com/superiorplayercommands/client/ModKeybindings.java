package com.superiorplayercommands.client;

import com.superiorplayercommands.client.gui.ModSettingsScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings {
    private static KeyBinding openSettingsKey;
    
    public static void register() {
        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.superior-player-commands.open_settings",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_COMMA, // Default: comma key
            "category.superior-player-commands"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettingsKey.wasPressed()) {
                openSettingsScreen(client);
            }
        });
    }
    
    public static void openSettingsScreen(MinecraftClient client) {
        client.setScreen(new ModSettingsScreen(client.currentScreen));
    }
}
