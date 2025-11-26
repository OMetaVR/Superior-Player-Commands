package com.superiorplayercommands.network;

import com.superiorplayercommands.client.ClientStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class ClientStateReceiver {
    
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(StateSync.SYNC_STATE_PACKET, ClientStateReceiver::handleStateSync);
    }
    
    private static void handleStateSync(MinecraftClient client, ClientPlayNetworkHandler handler, 
            PacketByteBuf buf, PacketSender responseSender) {
        byte stateType = buf.readByte();
        boolean enabled = buf.readBoolean();
        float value = buf.readFloat();
        
        client.execute(() -> {
            switch (stateType) {
                case StateSync.STATE_NOCLIP -> {
                    ClientStateManager.setNoclipEnabled(enabled);
                    ClientStateManager.setNoclipSpeed(value);
                }
                case StateSync.STATE_WATERWALK -> ClientStateManager.setWaterwalkEnabled(enabled);
                case StateSync.STATE_MOBS_IGNORE -> ClientStateManager.setMobsIgnoreEnabled(enabled);
                case StateSync.STATE_FLY -> {
                    ClientStateManager.setFlyEnabled(enabled);
                    ClientStateManager.setFlySpeed(value);
                }
            }
        });
    }
}
