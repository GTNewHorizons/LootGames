package ru.timeconqueror.lootgames.api.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.api.block.tile.GameMasterTile;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.timecore.api.util.WorldUtils;

import java.util.function.BiFunction;

public class GameMasterBlock extends GameBlock implements IGameField {
    private final BiFunction<Integer, World, GameMasterTile<?>> tileEntityFactory;

    public GameMasterBlock(BiFunction<Integer, World, GameMasterTile<?>> tileEntityFactory) {
        this.tileEntityFactory = tileEntityFactory;
    }

    public GameMasterBlock(Material material, BiFunction<Integer, World, GameMasterTile<?>> tileEntityFactory) {
        super(material);
        this.tileEntityFactory = tileEntityFactory;
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        WorldUtils.forTypedTileWithWarn(worldIn, BlockPos.of(x, y, z), GameMasterTile.class, te -> te.onBlockRightClick(player, BlockPos.of(x, y, z)));

        return true;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public GameMasterTile<?> createTileEntity(World world, int metadata) {
        return tileEntityFactory.apply(metadata, world);
    }
}
