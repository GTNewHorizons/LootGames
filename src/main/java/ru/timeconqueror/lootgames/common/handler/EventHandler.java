package ru.timeconqueror.lootgames.common.handler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.timeconqueror.lootgames.api.block.ILeftInteractible;

@Mod.EventBusSubscriber
public class EventHandler {
    @SubscribeEvent
    public static void onBlockBreak(PlayerInteractEvent.LeftClickBlock event) {
        BlockState blockState = event.getWorld().getBlockState(event.getPos());
        Block block = blockState.getBlock();

        if (block instanceof ILeftInteractible) {
            event.setCanceled(((ILeftInteractible) block).onLeftClick(event.getWorld(), event.getPlayer(), event.getPos(), event.getFace()));
        }
    }
}