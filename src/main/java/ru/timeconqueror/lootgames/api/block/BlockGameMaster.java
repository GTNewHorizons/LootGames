package ru.timeconqueror.lootgames.api.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import ru.timeconqueror.lootgames.api.block.tile.TileEntityGameMaster;
import ru.timeconqueror.lootgames.utils.WorldUtils;

public abstract class BlockGameMaster extends BlockGame implements ILeftInteractible {

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @NotNull
    public abstract TileEntityGameMaster<?> createTileEntity(BlockState state, IBlockReader world);

    @Override
    public boolean onLeftClick(World world, PlayerEntity player, BlockPos pos, Direction face) {
        if (face == Direction.UP) {
            handleLeftClick(player, world, pos, pos, face);

            return true;
        }

        return false;
    }

    public static void handleLeftClick(PlayerEntity player, World world, BlockPos masterPos, BlockPos subordinatePos, Direction face) {
        if (!world.isClientSide()) {
            WorldUtils.forTypedTileWithWarn(world, masterPos, TileEntityGameMaster.class, master -> {
                master.onBlockLeftClick((ServerPlayerEntity) player, subordinatePos);
            });
        }
    }
}
