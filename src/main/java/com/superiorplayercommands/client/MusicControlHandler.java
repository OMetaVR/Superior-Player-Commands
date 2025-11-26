package com.superiorplayercommands.client;

import com.superiorplayercommands.network.MusicControlPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;

@Environment(EnvType.CLIENT)
public class MusicControlHandler {
    
    private static boolean isPaused = false;
    private static float musicVolume = 1.0f;
    
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(MusicControlPacket.MUSIC_CONTROL_PACKET, (client, handler, buf, responseSender) -> {
            String action = buf.readString();
            float volume = buf.readFloat();
            
            client.execute(() -> {
                handleMusicControl(action, volume);
            });
        });
    }
    
    private static void handleMusicControl(String action, float volume) {
        MinecraftClient client = MinecraftClient.getInstance();
        SoundManager soundManager = client.getSoundManager();
        
        switch (action) {
            case "pause" -> {
                soundManager.pauseAll();
                isPaused = true;
            }
            case "play" -> {
                if (isPaused) {
                    soundManager.resumeAll();
                    isPaused = false;
                }
                if (volume >= 0) {
                    setMusicVolume(volume);
                }
            }
            case "skip" -> {
                soundManager.stopAll();
                isPaused = false;
            }
            case "back" -> {
                // just restarts the current music (didn't find a way to go back through them)
                soundManager.stopAll();
                isPaused = false;
            }
            case "volume" -> {
                setMusicVolume(volume);
            }
        }
    }
    
    private static void setMusicVolume(float volume) {
        musicVolume = Math.max(0, Math.min(1, volume));
        MinecraftClient client = MinecraftClient.getInstance();
        client.options.getSoundVolumeOption(SoundCategory.MUSIC)
            .setValue((double) musicVolume);
    }
    
    public static float getMusicVolume() {
        return musicVolume;
    }
}
