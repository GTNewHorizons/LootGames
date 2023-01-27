package ru.timeconqueror.lootgames.api.packet;

import java.io.IOException;
import net.minecraft.network.PacketBuffer;

public interface IGamePacket {
    void encode(PacketBuffer bufferTo) throws IOException;

    void decode(PacketBuffer bufferFrom) throws IOException;
}
