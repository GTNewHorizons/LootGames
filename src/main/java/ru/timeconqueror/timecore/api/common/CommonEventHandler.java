package ru.timeconqueror.timecore.api.common;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import ru.timeconqueror.lootgames.api.block.ILeftInteractible;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.sanity.InteractSide;

public class CommonEventHandler {
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block block = event.world.getBlock(event.x, event.y, event.z);

        if (block instanceof ILeftInteractible) {
            event.setCanceled(((ILeftInteractible) block).onLeftClick(event.world, event.entityPlayer, BlockPos.of(event.x, event.y, event.z), InteractSide.byFace(event.face).getFacing()));
        }
    }
}
