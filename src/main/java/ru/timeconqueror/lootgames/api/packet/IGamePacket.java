package ru.timeconqueror.lootgames.api.packet;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public interface IGamePacket {
    void encode(PacketBuffer bufferTo) throws IOException;

    void decode(PacketBuffer bufferFrom) throws IOException;
}
