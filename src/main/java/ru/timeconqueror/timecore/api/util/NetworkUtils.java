package ru.timeconqueror.timecore.api.util;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IChatComponent;
import ru.timeconqueror.lootgames.utils.future.BlockPos;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NetworkUtils {

    /**
     * Send provided message for provided player.
     */
    public static void sendMessage(EntityPlayer player, IChatComponent component) {
        player.addChatComponentMessage(component);
    }

    /**
     * Send provided message for each player, who is in specific distance from given pos
     */
    public static void sendForEachPlayerNearby(BlockPos fromPos, double distanceIn, IChatComponent component) {
        forEachPlayerNearby(fromPos, distanceIn, serverPlayerEntity -> sendMessage(serverPlayerEntity, component));
    }

    /**
     * Do provided action for each player, who is in specific distance from given pos.\
     */
    public static void forEachPlayerNearby(BlockPos fromPos, double distanceIn, Consumer<EntityPlayerMP> action) {
        for (EntityPlayerMP player : getPlayersNearby(fromPos, distanceIn)) {
            action.accept(player);
        }
    }

    /**
     * Returns all players who are in specific distance from given pos.
     */
    public static List<EntityPlayerMP> getPlayersNearby(BlockPos fromPos, double distanceIn) {
        @SuppressWarnings("unchecked")
        List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
        return players.stream()
                .filter(player -> {
                    double distanceSq = player.getDistanceSq(fromPos.getX(), fromPos.getY(), fromPos.getZ());
                    return distanceIn * distanceIn >= distanceSq;
                })
                .collect(Collectors.toList());
    }
}
