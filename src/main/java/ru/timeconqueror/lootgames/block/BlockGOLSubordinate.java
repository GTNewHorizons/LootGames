package ru.timeconqueror.lootgames.block;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import ru.timeconqueror.lootgames.api.block.BlockGame;
import ru.timeconqueror.lootgames.api.util.DirectionOctagonal;
import ru.timeconqueror.lootgames.minigame.gameoflight.TileEntityGOLMaster;

//TODO move to minigame
public class BlockGOLSubordinate extends BlockGame {
    public static final PropertyEnum<DirectionOctagonal> OFFSET = PropertyEnum.create("offset", DirectionOctagonal.class);

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(OFFSET, DirectionOctagonal.byIndex(meta));
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, OFFSET);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(OFFSET).getIndex();
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos masterPos = state.getValue(OFFSET).getMasterBlockPos(pos);
        TileEntity te = worldIn.getTileEntity(masterPos);
        if (te instanceof TileEntityGOLMaster) {
            TileEntityGOLMaster master = ((TileEntityGOLMaster) te);
            master.onSubordinateClickedByPlayer(state.getValue(OFFSET), playerIn);
            return true;
        }

        return false;
    }
}
