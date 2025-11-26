package com.superiorplayercommands;

import com.superiorplayercommands.bind.BindManager;
import com.superiorplayercommands.bind.KeyHelper;
import com.superiorplayercommands.client.ClientStateManager;
import com.superiorplayercommands.network.ClientStateReceiver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class SuperiorPlayerCommandsClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        SuperiorPlayerCommands.LOGGER.info("Superior Player Commands client initializing...");
        
        BindManager.load();
        
        ClientStateReceiver.register();
        
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientStateManager.reset();
        });
        
        SuperiorPlayerCommands.LOGGER.info("Superior Player Commands client initialized!");
    }
    
    private void onClientTick(MinecraftClient client) {
        if (client.world == null || client.player == null || client.currentScreen != null) {
            return;
        }
        
        long windowHandle = client.getWindow().getHandle();
        
        for (Map.Entry<String, String> entry : BindManager.getAllBindings().entrySet()) {
            String keyName = entry.getKey();
            String command = entry.getValue();
            
            int keyCode = KeyHelper.getKeyCode(keyName);
            if (keyCode == -1) continue;
            
            boolean isPressed = GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS;
            
            if (BindManager.isKeyJustPressed(keyName, isPressed)) {
                executeCommand(client, command);
            }
        }
    }
    
    private void executeCommand(MinecraftClient client, String command) {
        if (client.player == null) return;
        
        String cmd = command.startsWith("/") ? command.substring(1) : command;
        
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendChatCommand(cmd);
        }
    }
}



