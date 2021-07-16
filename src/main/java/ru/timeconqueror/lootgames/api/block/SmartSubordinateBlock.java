package ru.timeconqueror.lootgames.api.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.api.block.tile.GameMasterTile;
import ru.timeconqueror.lootgames.client.IconLoader;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.timecore.api.util.WorldUtils;

import java.util.function.BiConsumer;

/**
 * Subordinate block for minigames. Will find master block and notify it. The master block must be at the north-west corner of the game border
 * and its tileentity must extend {@link GameMasterTile <>}!
 */
public class SmartSubordinateBlock extends GameBlock implements ILeftInteractible, ISubordinateProvider {
    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        if (!worldIn.isRemote) {
            LootGamesAPI.getFieldManager().onFieldBlockBroken(worldIn, () -> getMasterPos(worldIn, BlockPos.of(x, y, z)));
        }

        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        BlockPos pos = BlockPos.of(x, y, z);
        forMasterTile(player, worldIn, pos, (master, blockPos) -> master.onBlockRightClick(player, pos));

        return true;
    }

    @Override
    public boolean onLeftClick(World world, EntityPlayer player, BlockPos pos, EnumFacing face) {
        if (face == EnumFacing.UP && !player.isSneaking()) {
            forMasterTile(player, world, pos, (master, masterPos) -> master.onBlockLeftClick(player, pos));
            return true;
        }

        return false;
    }

    private void forMasterTile(EntityPlayer player, World world, BlockPos pos, BiConsumer<GameMasterTile<?>, BlockPos> action) {
        BlockPos masterPos = getMasterPos(world, pos);
        WorldUtils.forTypedTileWithWarn(player, world, masterPos, GameMasterTile.class, master -> action.accept(master, masterPos));
    }

    public static BlockPos getMasterPos(World world, BlockPos pos) {
        BlockPos.Mutable currentPos = pos.mutable();
        int limit = 128;

        while (currentPos.equals(pos) || WorldExt.getBlock(world, currentPos) instanceof ISubordinateProvider) {
            currentPos.move(-1, 0, 0);
            if (--limit == 0) break;
        }
        currentPos.move(1, 0, 0);

        while (currentPos.equals(pos) || WorldExt.getBlock(world, currentPos) instanceof ISubordinateProvider) {
            currentPos.move(0, 0, -1);
            if (--limit == 0) break;
        }
        currentPos.move(0, 0, 1);

        // moving to corner, because master is there
        return currentPos.move(-1, 0, -1).immutable();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return IconLoader.shieldedDungeonFloor;
    }
}
