package com.superiorplayercommands.data;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager {
    
    private static final Map<UUID, Boolean> instamineEnabled = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastInstamineBreak = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastInstaminePos = new ConcurrentHashMap<>();
    private static final long INSTAMINE_COOLDOWN_MS = 100;
    private static final Map<UUID, Boolean> dropsEnabled = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> handsLevel = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> godModeEnabled = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> flyEnabled = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> flySpeed = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> noclipEnabled = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> mobsIgnoreEnabled = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> autosmeltEnabled = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> waterwalkEnabled = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> knockbackMultiplier = new ConcurrentHashMap<>();
    private static final Map<UUID, DeathPosition> lastDeathPosition = new ConcurrentHashMap<>();
    
    public static class DeathPosition {
        public final BlockPos pos;
        public final Identifier dimension;
        
        public DeathPosition(BlockPos pos, Identifier dimension) {
            this.pos = pos;
            this.dimension = dimension;
        }
    }
    
    public enum ToolTier {
        NONE(0, "none", 0),
        WOOD(1, "wood", 0),
        STONE(2, "stone", 1),
        IRON(3, "iron", 2),
        GOLD(4, "gold", 0),
        DIAMOND(5, "diamond", 3),
        NETHERITE(6, "netherite", 4);
        
        public final int level;
        public final String name;
        public final int harvestLevel;
        
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
                case GOLD -> 12.0f;
                case DIAMOND -> 8.0f;
                case NETHERITE -> 9.0f;
            };
        }
    }
    
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
    
    public static boolean isInstamineReady(UUID playerUuid, long packedPos) {
        if (!isInstamineEnabled(playerUuid)) return false;
        
        Long lastPos = lastInstaminePos.get(playerUuid);
        
        if (lastPos != null && lastPos == packedPos) {
            return true;
        }
        
        long lastBreak = lastInstamineBreak.getOrDefault(playerUuid, 0L);
        long elapsed = System.currentTimeMillis() - lastBreak;
        boolean ready = elapsed >= INSTAMINE_COOLDOWN_MS;
        
        return ready;
    }
    
    public static void updateInstaminePosition(UUID playerUuid, long packedPos) {
        Long lastPos = lastInstaminePos.get(playerUuid);
        
        if (lastPos == null || lastPos != packedPos) {
            lastInstamineBreak.put(playerUuid, System.currentTimeMillis());
            lastInstaminePos.put(playerUuid, packedPos);
        }
    }
    
    public static boolean areDropsEnabled(UUID playerUuid) {
        return dropsEnabled.getOrDefault(playerUuid, true);
    }
    
    public static void setDropsEnabled(UUID playerUuid, boolean enabled) {
        if (enabled) {
            dropsEnabled.remove(playerUuid);
        } else {
            dropsEnabled.put(playerUuid, false);
        }
    }
    
    public static boolean toggleDrops(UUID playerUuid) {
        boolean newState = !areDropsEnabled(playerUuid);
        setDropsEnabled(playerUuid, newState);
        return newState;
    }
    
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
    
    public static boolean isGodModeEnabled(UUID playerUuid) {
        return godModeEnabled.getOrDefault(playerUuid, false);
    }
    
    public static boolean toggleGodMode(UUID playerUuid) {
        boolean newState = !isGodModeEnabled(playerUuid);
        if (newState) {
            godModeEnabled.put(playerUuid, true);
        } else {
            godModeEnabled.remove(playerUuid);
        }
        return newState;
    }
    
    public static boolean isFlyEnabled(UUID playerUuid) {
        return flyEnabled.getOrDefault(playerUuid, false);
    }
    
    public static void setFlyEnabled(UUID playerUuid, boolean enabled) {
        if (enabled) {
            flyEnabled.put(playerUuid, true);
        } else {
            flyEnabled.remove(playerUuid);
        }
    }
    
    public static boolean toggleFly(UUID playerUuid) {
        boolean newState = !isFlyEnabled(playerUuid);
        setFlyEnabled(playerUuid, newState);
        return newState;
    }
    
    public static float getFlySpeed(UUID playerUuid) {
        return flySpeed.getOrDefault(playerUuid, 0.05f);
    }
    
    public static void setFlySpeed(UUID playerUuid, float speed) {
        flySpeed.put(playerUuid, speed);
    }
    
    public static boolean isNoclipEnabled(UUID playerUuid) {
        return noclipEnabled.getOrDefault(playerUuid, false);
    }
    
    public static boolean toggleNoclip(UUID playerUuid) {
        boolean newState = !isNoclipEnabled(playerUuid);
        if (newState) {
            noclipEnabled.put(playerUuid, true);
        } else {
            noclipEnabled.remove(playerUuid);
        }
        return newState;
    }
    
    public static boolean isMobsIgnoreEnabled(UUID playerUuid) {
        return mobsIgnoreEnabled.getOrDefault(playerUuid, false);
    }
    
    public static boolean toggleMobsIgnore(UUID playerUuid) {
        boolean newState = !isMobsIgnoreEnabled(playerUuid);
        if (newState) {
            mobsIgnoreEnabled.put(playerUuid, true);
        } else {
            mobsIgnoreEnabled.remove(playerUuid);
        }
        return newState;
    }
    
    public static boolean isAutosmeltEnabled(UUID playerUuid) {
        return autosmeltEnabled.getOrDefault(playerUuid, false);
    }
    
    public static boolean toggleAutosmelt(UUID playerUuid) {
        boolean newState = !isAutosmeltEnabled(playerUuid);
        if (newState) {
            autosmeltEnabled.put(playerUuid, true);
        } else {
            autosmeltEnabled.remove(playerUuid);
        }
        return newState;
    }
    
    public static boolean isWaterwalkEnabled(UUID playerUuid) {
        return waterwalkEnabled.getOrDefault(playerUuid, false);
    }
    
    public static boolean toggleWaterwalk(UUID playerUuid) {
        boolean newState = !isWaterwalkEnabled(playerUuid);
        if (newState) {
            waterwalkEnabled.put(playerUuid, true);
        } else {
            waterwalkEnabled.remove(playerUuid);
        }
        return newState;
    }
    
    public static float getKnockbackMultiplier(UUID playerUuid) {
        return knockbackMultiplier.getOrDefault(playerUuid, 1.0f);
    }
    
    public static void setKnockbackMultiplier(UUID playerUuid, float multiplier) {
        if (multiplier == 1.0f) {
            knockbackMultiplier.remove(playerUuid);
        } else {
            knockbackMultiplier.put(playerUuid, multiplier);
        }
    }
    
    public static boolean hasKnockbackModifier(UUID playerUuid) {
        return knockbackMultiplier.containsKey(playerUuid);
    }
    
    public static void setLastDeathPosition(UUID playerUuid, BlockPos pos, Identifier dimension) {
        lastDeathPosition.put(playerUuid, new DeathPosition(pos, dimension));
    }
    
    public static DeathPosition getLastDeathPosition(UUID playerUuid) {
        return lastDeathPosition.get(playerUuid);
    }
    
    public static void clearPlayerState(UUID playerUuid) {
        instamineEnabled.remove(playerUuid);
        lastInstamineBreak.remove(playerUuid);
        lastInstaminePos.remove(playerUuid);
        dropsEnabled.remove(playerUuid);
        handsLevel.remove(playerUuid);
        godModeEnabled.remove(playerUuid);
        flyEnabled.remove(playerUuid);
        flySpeed.remove(playerUuid);
        noclipEnabled.remove(playerUuid);
        mobsIgnoreEnabled.remove(playerUuid);
        autosmeltEnabled.remove(playerUuid);
        waterwalkEnabled.remove(playerUuid);
        knockbackMultiplier.remove(playerUuid);
    }
}

