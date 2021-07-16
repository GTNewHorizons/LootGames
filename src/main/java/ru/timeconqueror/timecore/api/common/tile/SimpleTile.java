package ru.timeconqueror.timecore.api.common.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.BlockState;
import ru.timeconqueror.lootgames.utils.future.WorldExt;

import java.util.Objects;

/**
 * Tile entity with some useful methods
 */
public abstract class SimpleTile extends TileEntity {
    public boolean isServerSide() {
        return !isClientSide();
    }

    public boolean isClientSide() {
        World level = Objects.requireNonNull(getWorld());
        return level.isRemote;
    }

    /**
     * Returns the blockstate on tileentity pos.
     */
    public BlockState getState() {
        World level = Objects.requireNonNull(getWorld());

        return WorldExt.getBlockState(level, BlockPos.of(xCoord, yCoord, zCoord));
    }

    public BlockPos getBlockPos() {
        return BlockPos.of(xCoord, yCoord, zCoord);
    }
}
