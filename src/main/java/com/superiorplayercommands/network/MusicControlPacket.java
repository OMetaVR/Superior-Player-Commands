package com.superiorplayercommands.network;

import com.superiorplayercommands.SuperiorPlayerCommands;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MusicControlPacket {
    
    public static final Identifier MUSIC_CONTROL_PACKET = new Identifier(SuperiorPlayerCommands.MOD_ID, "music_control");
    
    public static void send(ServerPlayerEntity player, String action, float volume) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(action);
        buf.writeFloat(volume);
        
        ServerPlayNetworking.send(player, MUSIC_CONTROL_PACKET, buf);
    }
}
