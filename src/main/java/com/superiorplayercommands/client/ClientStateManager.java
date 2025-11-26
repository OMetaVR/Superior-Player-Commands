package com.superiorplayercommands.client;

import net.minecraft.util.math.Vec3d;

public class ClientStateManager {
    
    private static boolean noclipEnabled = false;
    private static float noclipSpeed = 1.0f;
    private static boolean waterwalkEnabled = false;
    private static boolean mobsIgnoreEnabled = false;
    private static boolean flyEnabled = false;
    private static float flySpeed = 1.0f;
    
    public static boolean isNoclipEnabled() {
        return noclipEnabled;
    }
    
    public static void setNoclipEnabled(boolean enabled) {
        noclipEnabled = enabled;
    }
    
    public static float getNoclipSpeed() {
        return noclipSpeed;
    }
    
    public static void setNoclipSpeed(float speed) {
        noclipSpeed = speed;
    }
    
    public static boolean isWaterwalkEnabled() {
        return waterwalkEnabled;
    }
    
    public static void setWaterwalkEnabled(boolean enabled) {
        waterwalkEnabled = enabled;
    }
    
    public static boolean isMobsIgnoreEnabled() {
        return mobsIgnoreEnabled;
    }
    
    public static void setMobsIgnoreEnabled(boolean enabled) {
        mobsIgnoreEnabled = enabled;
    }
    
    public static boolean isFlyEnabled() {
        return flyEnabled;
    }
    
    public static void setFlyEnabled(boolean enabled) {
        flyEnabled = enabled;
    }
    
    public static float getFlySpeed() {
        return flySpeed;
    }
    
    public static void setFlySpeed(float speed) {
        flySpeed = speed;
    }
    
    public static void reset() {
        noclipEnabled = false;
        noclipSpeed = 1.0f;
        waterwalkEnabled = false;
        mobsIgnoreEnabled = false;
        flyEnabled = false;
        flySpeed = 1.0f;
    }
}
