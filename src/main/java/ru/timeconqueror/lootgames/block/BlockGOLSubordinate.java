package ru.timeconqueror.lootgames.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.tileentity.TileEntityGOLMaster;
import ru.timeconqueror.lootgames.tileentity.TileEntityGOLSubordinate;

import javax.annotation.Nullable;

public class BlockGOLSubordinate extends Block {
    public static final PropertyEnum<EnumPosOffset> OFFSET = PropertyEnum.create("offset", EnumPosOffset.class);
    public static final PropertyBool ACTIVATED = PropertyBool.create("activated");

    public BlockGOLSubordinate() {
        super(Material.BARRIER);
        setBlockUnbreakable();
        setLightLevel(1.0F);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int index = meta > 7 ? meta - 8 : meta;
        boolean activated = meta > 7;
        return getDefaultState().withProperty(ACTIVATED, activated).withProperty(OFFSET, EnumPosOffset.byIndex(index));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTIVATED, OFFSET);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(OFFSET).getIndex();
        meta += state.getValue(ACTIVATED) ? 8 : 0;

        return meta;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityGOLSubordinate();
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

    public enum EnumPosOffset implements IStringSerializable {
        NORTH(0, "north", 0, -1),
        NORTH_EAST(1, "north_east", 1, -1),
        EAST(2, "east", 1, 0),
        SOUTH_EAST(3, "south_east", 1, 1),
        SOUTH(4, "south", 0, 1),
        SOUTH_WEST(5, "south_west", -1, 1),
        WEST(6, "west", -1, 0),
        NORTH_WEST(7, "north_west", -1, -1);

        private static final EnumPosOffset[] LOOKUP = new EnumPosOffset[EnumPosOffset.values().length];

        static {
            for (EnumPosOffset value : values()) {
                LOOKUP[value.index] = value;
            }
        }

        private final String name;
        private final int index;
        /**
         * Offset from master block
         */
        private final int offsetX;
        /**
         * Offset from master block
         */
        private final int offsetZ;

        EnumPosOffset(int index, String name, int offsetX, int offsetZ) {
            this.index = index;
            this.name = name;
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
        }

        public static EnumPosOffset byIndex(int index) {
            return LOOKUP[index];
        }

        @Override
        public String getName() {
            return name;
        }

        public BlockPos getMasterBlockPos(BlockPos subordinatePos) {
            return subordinatePos.add(offsetX != 0 ? -offsetX : 0, 0, offsetZ != 0 ? -offsetZ : 0);
        }

        public int getIndex() {
            return index;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public int getOffsetZ() {
            return offsetZ;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    //    @Override
//    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
//        if (worldIn.isRemote)
//            return true;
//        else {
//            if (worldIn.getTileEntity(pos) instanceof TileEntityGameOfLight) {
//                TileEntityGameOfLight tileEntity = (TileEntityGameOfLight) worldIn.getTileEntity(pos);
//
//                ItemStack inhandStack = null;
//                //TODO ?
////                if (LootGames.Donors != null && LootGames.Donors.isDonor(pPlayer))
////                    inhandStack = pPlayer.getCurrentEquippedItem();
//
////                if (inhandStack != null && inhandStack.getItem() == Items.diamond)
////                    tileEntity.toggleMusicMode(pPlayer);
////                else
////                    tileEntity.onBlockClickedByPlayer(null, pPlayer);
//            }
//            return true;
//        }
//    }
}
