package ru.timeconqueror.lootgames.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.timecore.api.util.client.ClientProxy;

public class ClientEventHandler {
    @SubscribeEvent
    @SideOnly(value = Side.CLIENT)
    public void disableSubordinateHighlight(DrawBlockHighlightEvent event) {
        MovingObjectPosition target = event.target;
        if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            World world = ClientProxy.world();
            Block block = world.getBlock(target.blockX, target.blockY, target.blockZ);
            if (block == LGBlocks.SMART_SUBORDINATE) {
                event.setCanceled(true);
            }
        }
    }
}
