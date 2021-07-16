package ru.timeconqueror.lootgames.common.packet;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import ru.timeconqueror.lootgames.LootGames;

public class LGNetwork {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(LootGames.MODID + "2");

    public static void init() {
        INSTANCE.registerMessage(SPacketGameUpdate.makeHandler(), SPacketGameUpdate.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(CPacketGameUpdate.makeHandler(), CPacketGameUpdate.class, 1, Side.SERVER);
    }
}
