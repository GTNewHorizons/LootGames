package ru.timeconqueror.lootgames.utils;

import java.util.function.Consumer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.world.WorldServer;

public class Trackers {
    public static void forPlayersWatchingChunk(WorldServer world, int x, int z, Consumer<EntityPlayerMP> action) {
        PlayerManager playerManager = world.getPlayerManager();
        PlayerManager.PlayerInstance instance = playerManager.getOrCreateChunkWatcher(x, z, false);

        if (instance != null) {
            for (Object o : instance.playersWatchingChunk) {
                EntityPlayerMP player = (EntityPlayerMP) o;
                if (!player.loadedChunks.contains(instance.chunkLocation)) {
                    action.accept(player);
                }
            }
        }
    }
}
