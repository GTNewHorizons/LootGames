package ru.timeconqueror.lootgames.api.packet;

import ru.timeconqueror.lootgames.LootGames;

public class GamePacketRegister {
    private static int clientIncrement = 0;
    private static int serverIncrement = 0;

    public static void regClientPacket(Class<? extends IClientGamePacket> packetClass) {
        GamePacketRegistry.clientStorage().regPacket(LootGames.MODID, clientIncrement++, packetClass);
    }

    public static void regServerPacket(Class<? extends IServerGamePacket> packetClass) {
        GamePacketRegistry.serverStorage().regPacket(LootGames.MODID, serverIncrement++, packetClass);
    }
}
