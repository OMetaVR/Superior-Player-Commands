package com.superiorplayercommands.alias;

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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AliasManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path ALIASES_FILE = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("superior-player-commands-aliases.json");
    
    private static Map<String, String> aliases = new LinkedHashMap<>();
    private static final Map<String, String> DEFAULT_ALIASES = new LinkedHashMap<>();
    
    static {
        DEFAULT_ALIASES.put("gmc", "gamemode creative");
        DEFAULT_ALIASES.put("gms", "gamemode survival");
        DEFAULT_ALIASES.put("gma", "gamemode adventure");
        DEFAULT_ALIASES.put("gmsp", "gamemode spectator");
    }
    
    public static void load() {
        if (Files.exists(ALIASES_FILE)) {
            try (Reader reader = Files.newBufferedReader(ALIASES_FILE)) {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    aliases = new LinkedHashMap<>(loaded);
                    SuperiorPlayerCommands.LOGGER.info("Loaded {} aliases", aliases.size());
                }
            } catch (IOException e) {
                SuperiorPlayerCommands.LOGGER.error("Failed to load aliases", e);
            }
        } else {
            aliases = new LinkedHashMap<>(DEFAULT_ALIASES);
            save();
            SuperiorPlayerCommands.LOGGER.info("Created default aliases");
        }
    }
    
    public static void save() {
        try {
            Files.createDirectories(ALIASES_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(ALIASES_FILE)) {
                GSON.toJson(aliases, writer);
            }
        } catch (IOException e) {
            SuperiorPlayerCommands.LOGGER.error("Failed to save aliases", e);
        }
    }
    
    public static void setAlias(String name, String command) {
        aliases.put(name.toLowerCase(), command);
        save();
    }
    
    public static boolean removeAlias(String name) {
        String removed = aliases.remove(name.toLowerCase());
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }
    
    public static Optional<String> getAlias(String name) {
        return Optional.ofNullable(aliases.get(name.toLowerCase()));
    }
    
    public static Map<String, String> getAllAliases() {
        return new LinkedHashMap<>(aliases);
    }
    
    public static boolean hasAlias(String name) {
        return aliases.containsKey(name.toLowerCase());
    }
    
    public static void resetToDefaults() {
        aliases = new LinkedHashMap<>(DEFAULT_ALIASES);
        save();
    }
    
    public static Map<String, String> getDefaultAliases() {
        return new LinkedHashMap<>(DEFAULT_ALIASES);
    }
}
