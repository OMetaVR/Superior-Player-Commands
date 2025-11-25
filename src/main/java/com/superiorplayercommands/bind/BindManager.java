package com.superiorplayercommands.bind;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.superiorplayercommands.SuperiorPlayerCommands;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages key bindings for commands
 * Stores bindings as key name -> full command string
 */
public class BindManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path BINDS_FILE = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("superior-player-commands-binds.json");
    
    // Map of GLFW key name (lowercase) -> command string
    private static Map<String, String> bindings = new HashMap<>();
    
    // Track which keys are currently held to prevent repeat firing
    private static Map<String, Boolean> keyStates = new HashMap<>();
    
    public static void load() {
        if (Files.exists(BINDS_FILE)) {
            try (Reader reader = Files.newBufferedReader(BINDS_FILE)) {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    bindings = loaded;
                    SuperiorPlayerCommands.LOGGER.info("Loaded {} key bindings", bindings.size());
                }
            } catch (IOException e) {
                SuperiorPlayerCommands.LOGGER.error("Failed to load bindings", e);
            }
        }
    }
    
    public static void save() {
        try {
            Files.createDirectories(BINDS_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(BINDS_FILE)) {
                GSON.toJson(bindings, writer);
            }
        } catch (IOException e) {
            SuperiorPlayerCommands.LOGGER.error("Failed to save bindings", e);
        }
    }
    
    /**
     * Add or update a binding
     * @param key The key name (e.g., "j", "k", "f6")
     * @param command The full command to execute (without leading slash)
     */
    public static void setBind(String key, String command) {
        bindings.put(key.toLowerCase(), command);
        save();
    }
    
    /**
     * Remove a binding
     */
    public static boolean removeBind(String key) {
        String removed = bindings.remove(key.toLowerCase());
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }
    
    /**
     * Get the command bound to a key
     */
    public static Optional<String> getBind(String key) {
        return Optional.ofNullable(bindings.get(key.toLowerCase()));
    }
    
    /**
     * Get all bindings
     */
    public static Map<String, String> getAllBindings() {
        return new HashMap<>(bindings);
    }
    
    /**
     * Check if a key was just pressed (not held)
     */
    public static boolean isKeyJustPressed(String key, boolean currentlyPressed) {
        String keyLower = key.toLowerCase();
        boolean wasPressed = keyStates.getOrDefault(keyLower, false);
        keyStates.put(keyLower, currentlyPressed);
        
        // Return true only on the transition from not pressed to pressed
        return currentlyPressed && !wasPressed;
    }
    
    /**
     * Clear the pressed state for a key
     */
    public static void clearKeyState(String key) {
        keyStates.put(key.toLowerCase(), false);
    }
}

