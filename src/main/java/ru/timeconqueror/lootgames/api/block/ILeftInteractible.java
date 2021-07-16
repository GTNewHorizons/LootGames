package ru.timeconqueror.lootgames.api.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.utils.future.BlockPos;

public interface ILeftInteractible {
    /**
     * Should return true, if you need to prevent left click.
     */
    boolean onLeftClick(World world, EntityPlayer player, BlockPos pos, EnumFacing face);
}
