package com.superiorplayercommands.config;

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
import java.util.LinkedHashMap;
import java.util.Map;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("superior-player-commands-config.json");
    
    private static ConfigData config = new ConfigData();
    
    public static class ConfigData {
        public boolean masterEnabled = true;
        public Map<String, Boolean> commandToggles = new LinkedHashMap<>();
    }
    
    // All available commands organized by category
    public static final Map<String, String[]> COMMAND_CATEGORIES = new LinkedHashMap<>();
    
    static {
        COMMAND_CATEGORIES.put("Core", new String[]{
            "jump", "ascend", "descend", "back", "return", "unstuck",
            "bind", "alias", "hideresponses", "useportal", "help",
            "set", "rem", "goto", "listwaypoints"
        });
        COMMAND_CATEGORIES.put("Inventory", new String[]{
            "heal", "hunger", "replenish", "repair", "destroy",
            "duplicate", "more", "stack", "ench", "drop", "dropstore"
        });
        COMMAND_CATEGORIES.put("Power", new String[]{
            "god", "fly", "noclip", "mobsignore", "instamine", "drops",
            "hands", "autosmelt", "waterwalk", "knockback", "fullbright",
            "setjump", "setspeed", "falldamage", "firedamage", "drowndamage",
            "health", "ride"
        });
        COMMAND_CATEGORIES.put("World", new String[]{
            "extinguish", "freeze", "freezeai", "killall", "explode",
            "lightning", "defuse", "grow", "spawnstack"
        });
        COMMAND_CATEGORIES.put("Utility", new String[]{
            "calc", "biome", "measure", "tps", "coords", "music"
        });
    }
    
    public static void load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                Type type = new TypeToken<ConfigData>(){}.getType();
                ConfigData loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    config = loaded;
                    SuperiorPlayerCommands.LOGGER.info("Loaded mod config");
                }
            } catch (IOException e) {
                SuperiorPlayerCommands.LOGGER.error("Failed to load config", e);
            }
        }
        
        // Ensure all commands have a toggle entry (default enabled)
        for (String[] commands : COMMAND_CATEGORIES.values()) {
            for (String cmd : commands) {
                config.commandToggles.putIfAbsent(cmd, true);
            }
        }
        save();
    }
    
    public static void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            SuperiorPlayerCommands.LOGGER.error("Failed to save config", e);
        }
    }
    
    public static boolean isMasterEnabled() {
        return config.masterEnabled;
    }
    
    public static void setMasterEnabled(boolean enabled) {
        config.masterEnabled = enabled;
        save();
    }
    
    public static boolean isCommandEnabled(String command) {
        if (!config.masterEnabled) return false;
        return config.commandToggles.getOrDefault(command.toLowerCase(), true);
    }
    
    public static void setCommandEnabled(String command, boolean enabled) {
        config.commandToggles.put(command.toLowerCase(), enabled);
        save();
    }
    
    public static Map<String, Boolean> getAllToggles() {
        return new LinkedHashMap<>(config.commandToggles);
    }
    
    public static void enableAll() {
        for (String cmd : config.commandToggles.keySet()) {
            config.commandToggles.put(cmd, true);
        }
        save();
    }
    
    public static void disableAll() {
        for (String cmd : config.commandToggles.keySet()) {
            config.commandToggles.put(cmd, false);
        }
        save();
    }
}
