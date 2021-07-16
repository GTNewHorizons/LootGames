package ru.timeconqueror.timecore.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.ChatComponentExt;
import ru.timeconqueror.lootgames.utils.future.WorldExt;

import java.util.function.Consumer;

public class WorldUtils {
    public static <T> void forTypedTile(World world, BlockPos pos, Class<T> clazz, Consumer<T> action) {
        forTypedTile(world, pos, clazz, action, s -> {
        });
    }

    public static <T> void forTypedTileWithWarn(EntityPlayer player, World world, BlockPos pos, Class<T> clazz, Consumer<T> action) {
        forTypedTile(world, pos, clazz, action, message -> {
            ChatComponentText text = ChatComponentExt.withStyle(new ChatComponentText(message), EnumChatFormatting.RED);
            NetworkUtils.sendMessage(player, text);

            LootGames.LOGGER.warn(message, new IllegalAccessException());
        });
    }

    public static <T> void forTypedTileWithWarn(World world, BlockPos pos, Class<T> clazz, Consumer<T> action) {
        forTypedTile(world, pos, clazz, action, message -> LootGames.LOGGER.warn(message, new IllegalAccessException()));
    }

    public static <T> void forTileWithReqt(World world, BlockPos pos, Class<T> clazz, Consumer<T> action) {
        forTypedTile(world, pos, clazz, action, error -> {
            throw new IllegalStateException(error);
        });
    }

    public static <T> void forTypedTile(World world, BlockPos pos, Class<T> clazz, Consumer<T> action, Consumer<String> errorHandler) {
        TileEntity tile = WorldExt.getTileEntity(world, pos);

        if (tile == null) {
            errorHandler.accept("Error. There's no tile on " + pos);
            return;
        }

        if (clazz.isInstance(tile)) {
            action.accept((T) tile);
        } else {
            errorHandler.accept("Error. There's a tile " + tile.getClass().getName() + " instead of " + clazz.getName() + " on " + pos);
        }
    }
}