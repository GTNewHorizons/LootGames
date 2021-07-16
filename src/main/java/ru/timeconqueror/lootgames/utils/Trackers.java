package ru.timeconqueror.lootgames.utils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.world.WorldServer;

import java.util.function.Consumer;

public class Trackers {
    public static void forPlayersWatchingChunk(WorldServer world, int x, int z, Consumer<EntityPlayerMP> action) {
        PlayerManager playerManager = world.getPlayerManager();
        PlayerManager.PlayerInstance instance = playerManager.getPlayerInstance(x, z, false);

        if (instance != null) {
            for (Object o : instance.playersWatchingChunk) {
                EntityPlayerMP player = (EntityPlayerMP) o;
                if (!player.loadedChunks.contains(instance.currentChunk)) {
                    action.accept(player);
                }
            }
        }
    }
}
