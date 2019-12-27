package ru.timeconqueror.lootgames.api.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
//TODO move to TimeCore
/**
 * @apiNote For use on logical server side <i>(when !world.isRemote)</i>.
 */
public class NetworkUtils {
    /**
     * Sends message to all players in given distance.
     *
     * @param distanceIn distance from {@code fromPos}, in which players will be get a message.
     *///TODO check it
    public static void sendMessageToAllNearby(BlockPos fromPos, ITextComponent msg, int distanceIn) {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            double distance = player.getDistance(fromPos.getX(), fromPos.getY(), fromPos.getZ());
            if (distance <= distanceIn) {
                player.sendMessage(msg);
            }
        }
    }

    //TODO add kotlin extension-fun variant (and colorWithAppendings)
    public static <T extends ITextComponent> T color(T component, TextFormatting color) {
        component.getStyle().setColor(color);
        return component;
    }
}