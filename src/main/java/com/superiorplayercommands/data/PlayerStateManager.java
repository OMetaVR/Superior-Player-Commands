package com.superiorplayercommands.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player toggle states for various features
 * These are session-only (not persisted) as they are cheats
 */
public class PlayerStateManager {
    
    // Instamine toggle
    private static final Map<UUID, Boolean> instamineEnabled = new ConcurrentHashMap<>();
    
    // Instamine cooldown tracking (last break time in milliseconds)
    private static final Map<UUID, Long> lastInstamineBreak = new ConcurrentHashMap<>();
    private static final long INSTAMINE_COOLDOWN_MS = 75; // 75ms delay between instant breaks
    
    // Drops toggle (false = drops disabled)
    private static final Map<UUID, Boolean> dropsEnabled = new ConcurrentHashMap<>();
    
    // Tool hands level (0 = disabled, 1 = wood, 2 = stone, 3 = iron, 4 = gold, 5 = diamond, 6 = netherite)
    private static final Map<UUID, Integer> handsLevel = new ConcurrentHashMap<>();
    
    public enum ToolTier {
        NONE(0, "none", 0),
        WOOD(1, "wood", 0),
        STONE(2, "stone", 1),
        IRON(3, "iron", 2),
        GOLD(4, "gold", 0), // Gold has same harvest level as wood but faster
        DIAMOND(5, "diamond", 3),
        NETHERITE(6, "netherite", 4);
        
        public final int level;
        public final String name;
        public final int harvestLevel; // Minecraft harvest level
        
        ToolTier(int level, String name, int harvestLevel) {
            this.level = level;
            this.name = name;
            this.harvestLevel = harvestLevel;
        }
        
        public static ToolTier fromLevel(int level) {
            for (ToolTier tier : values()) {
                if (tier.level == level) return tier;
            }
            return NONE;
        }
        
        public float getMiningSpeed() {
            return switch (this) {
                case NONE -> 1.0f;
                case WOOD -> 2.0f;
                case STONE -> 4.0f;
                case IRON -> 6.0f;
                case GOLD -> 12.0f; // Gold is fast but weak
                case DIAMOND -> 8.0f;
                case NETHERITE -> 9.0f;
            };
        }
    }
    
    // Instamine
    public static boolean isInstamineEnabled(UUID playerUuid) {
        return instamineEnabled.getOrDefault(playerUuid, false);
    }
    
    public static void setInstamineEnabled(UUID playerUuid, boolean enabled) {
        if (enabled) {
            instamineEnabled.put(playerUuid, true);
        } else {
            instamineEnabled.remove(playerUuid);
            lastInstamineBreak.remove(playerUuid);
        }
    }
    
    public static boolean toggleInstamine(UUID playerUuid) {
        boolean newState = !isInstamineEnabled(playerUuid);
        setInstamineEnabled(playerUuid, newState);
        return newState;
    }
    
    /**
     * Check if instamine is ready (cooldown passed)
     */
    public static boolean isInstamineReady(UUID playerUuid) {
        if (!isInstamineEnabled(playerUuid)) return false;
        
        long lastBreak = lastInstamineBreak.getOrDefault(playerUuid, 0L);
        return System.currentTimeMillis() - lastBreak >= INSTAMINE_COOLDOWN_MS;
    }
    
    /**
     * Record an instamine break for cooldown tracking
     */
    public static void recordInstamineBreak(UUID playerUuid) {
        lastInstamineBreak.put(playerUuid, System.currentTimeMillis());
    }
    
    // Drops
    public static boolean areDropsEnabled(UUID playerUuid) {
        return dropsEnabled.getOrDefault(playerUuid, true); // Default: drops enabled
    }
    
    public static void setDropsEnabled(UUID playerUuid, boolean enabled) {
        if (enabled) {
            dropsEnabled.remove(playerUuid); // Remove = default (enabled)
        } else {
            dropsEnabled.put(playerUuid, false);
        }
    }
    
    public static boolean toggleDrops(UUID playerUuid) {
        boolean newState = !areDropsEnabled(playerUuid);
        setDropsEnabled(playerUuid, newState);
        return newState;
    }
    
    // Tool hands
    public static ToolTier getHandsLevel(UUID playerUuid) {
        return ToolTier.fromLevel(handsLevel.getOrDefault(playerUuid, 0));
    }
    
    public static void setHandsLevel(UUID playerUuid, ToolTier tier) {
        if (tier == ToolTier.NONE) {
            handsLevel.remove(playerUuid);
        } else {
            handsLevel.put(playerUuid, tier.level);
        }
    }
    
    // Clean up when player disconnects
    public static void clearPlayerState(UUID playerUuid) {
        instamineEnabled.remove(playerUuid);
        lastInstamineBreak.remove(playerUuid);
        dropsEnabled.remove(playerUuid);
        handsLevel.remove(playerUuid);
    }
}

