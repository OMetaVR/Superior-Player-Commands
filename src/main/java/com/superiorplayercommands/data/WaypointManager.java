package com.superiorplayercommands.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.superiorplayercommands.SuperiorPlayerCommands;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path WAYPOINTS_FILE = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("superior-player-commands-waypoints.json");
    
    private static Map<String, Map<String, WaypointData>> playerWaypoints = new HashMap<>();
    
    public static class WaypointData {
        public int x, y, z;
        public String dimension;
        
        public WaypointData() {}
        
        public WaypointData(BlockPos pos, RegistryKey<World> dimension) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.dimension = dimension.getValue().toString();
        }
        
        public BlockPos getPos() {
            return new BlockPos(x, y, z);
        }
        
        public Identifier getDimensionId() {
            return new Identifier(dimension);
        }
    }
    
    public static void load() {
        if (Files.exists(WAYPOINTS_FILE)) {
            try (Reader reader = Files.newBufferedReader(WAYPOINTS_FILE)) {
                Type type = new TypeToken<Map<String, Map<String, WaypointData>>>(){}.getType();
                Map<String, Map<String, WaypointData>> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    playerWaypoints = loaded;
                    int count = playerWaypoints.values().stream().mapToInt(Map::size).sum();
                    SuperiorPlayerCommands.LOGGER.info("Loaded {} waypoints for {} players", count, playerWaypoints.size());
                }
            } catch (IOException e) {
                SuperiorPlayerCommands.LOGGER.error("Failed to load waypoints", e);
            }
        }
    }
    
    public static void save() {
        try {
            Files.createDirectories(WAYPOINTS_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(WAYPOINTS_FILE)) {
                GSON.toJson(playerWaypoints, writer);
            }
        } catch (IOException e) {
            SuperiorPlayerCommands.LOGGER.error("Failed to save waypoints", e);
        }
    }
    
    public static void setWaypoint(UUID playerUuid, String name, BlockPos pos, RegistryKey<World> dimension) {
        String uuid = playerUuid.toString();
        playerWaypoints.computeIfAbsent(uuid, k -> new HashMap<>());
        playerWaypoints.get(uuid).put(name.toLowerCase(), new WaypointData(pos, dimension));
        save();
    }
    
    public static boolean removeWaypoint(UUID playerUuid, String name) {
        String uuid = playerUuid.toString();
        Map<String, WaypointData> waypoints = playerWaypoints.get(uuid);
        if (waypoints != null) {
            WaypointData removed = waypoints.remove(name.toLowerCase());
            if (removed != null) {
                save();
                return true;
            }
        }
        return false;
    }
    
    public static Optional<WaypointData> getWaypoint(UUID playerUuid, String name) {
        String uuid = playerUuid.toString();
        Map<String, WaypointData> waypoints = playerWaypoints.get(uuid);
        if (waypoints != null) {
            return Optional.ofNullable(waypoints.get(name.toLowerCase()));
        }
        return Optional.empty();
    }
    
    public static Map<String, WaypointData> getWaypoints(UUID playerUuid) {
        String uuid = playerUuid.toString();
        return playerWaypoints.getOrDefault(uuid, new HashMap<>());
    }
    
    public static Collection<String> getWaypointNames(UUID playerUuid) {
        return getWaypoints(playerUuid).keySet();
    }
}



