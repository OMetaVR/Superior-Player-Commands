package com.superiorplayercommands.network;

import com.superiorplayercommands.SuperiorPlayerCommands;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class StateSync {
    
    public static final Identifier SYNC_STATE_PACKET = new Identifier(SuperiorPlayerCommands.MOD_ID, "sync_state");
    
    public static final byte STATE_NOCLIP = 0;
    public static final byte STATE_WATERWALK = 1;
    public static final byte STATE_MOBS_IGNORE = 2;
    public static final byte STATE_FLY = 3;
    
    public static void sendStateUpdate(ServerPlayerEntity player, byte stateType, boolean enabled) {
        sendStateUpdate(player, stateType, enabled, 1.0f);
    }
    
    public static void sendStateUpdate(ServerPlayerEntity player, byte stateType, boolean enabled, float value) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(stateType);
        buf.writeBoolean(enabled);
        buf.writeFloat(value);
        
        ServerPlayNetworking.send(player, SYNC_STATE_PACKET, buf);
    }
}
