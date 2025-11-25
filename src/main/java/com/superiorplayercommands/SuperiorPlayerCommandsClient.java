package com.superiorplayercommands;

import com.superiorplayercommands.bind.BindManager;
import com.superiorplayercommands.bind.KeyHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class SuperiorPlayerCommandsClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        SuperiorPlayerCommands.LOGGER.info("Superior Player Commands client initializing...");
        
        // Load saved bindings
        BindManager.load();
        
        // Register tick event to check for key presses
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        
        SuperiorPlayerCommands.LOGGER.info("Superior Player Commands client initialized!");
    }
    
    private void onClientTick(MinecraftClient client) {
        // Don't process binds if no world, or if a screen is open (typing in chat, etc.)
        if (client.world == null || client.player == null || client.currentScreen != null) {
            return;
        }
        
        long windowHandle = client.getWindow().getHandle();
        
        // Check all bindings
        for (Map.Entry<String, String> entry : BindManager.getAllBindings().entrySet()) {
            String keyName = entry.getKey();
            String command = entry.getValue();
            
            int keyCode = KeyHelper.getKeyCode(keyName);
            if (keyCode == -1) continue;
            
            boolean isPressed = GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS;
            
            // Only trigger on key down, not while held
            if (BindManager.isKeyJustPressed(keyName, isPressed)) {
                executeCommand(client, command);
            }
        }
    }
    
    private void executeCommand(MinecraftClient client, String command) {
        if (client.player == null) return;
        
        // Execute the command
        // If it starts with /, remove it (we'll add it back)
        String cmd = command.startsWith("/") ? command.substring(1) : command;
        
        // Send the command to the server
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendChatCommand(cmd);
        }
    }
}

