package ru.timeconqueror.lootgames.utils;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.world.WorldServer;

public class Trackers {

    public static void forPlayersWatchingChunk(WorldServer world, int x, int z, Consumer<EntityPlayerMP> action) {
        PlayerManager playerManager = world.getPlayerManager();
        for (Object o : world.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) o;
            if (playerManager.isPlayerWatchingChunk(player, x, z)) {
                action.accept(player);
            }
        }
    }
}
