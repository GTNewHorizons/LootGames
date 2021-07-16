
package eu.usrv.legacylootgames.gol.tiles;


import eu.usrv.legacylootgames.auxiliary.ExtendedDirections;
import eu.usrv.legacylootgames.blocks.DungeonBrick;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import ru.timeconqueror.lootgames.LegacyMigrator;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.WorldExt;

import static eu.usrv.legacylootgames.auxiliary.ExtendedDirections.UP;
import static eu.usrv.legacylootgames.auxiliary.ExtendedDirections.isHorizontal;


public class LegacyGameOfLightTile extends TileEntity {
    private static final String NBTTAG_ISACTIVE = "mIsActive";
    private static final String NBTTAG_ISMASTER = "mIsMaster";
    private static final String NBTTAG_DIRECTION = "mTEDirection";
    private static final String NBTTAG_BLOCK_NORTH = "mGameBlockNorth";
    private static final String NBTTAG_BLOCK_SOUTH = "mGameBlockSouth";
    private static final String NBTTAG_BLOCK_WEST = "mGameBlockEast";
    private static final String NBTTAG_BLOCK_EAST = "mGameBlockWest";
    private static final String NBTTAG_BLOCK_NW = "mGameBlockNW";
    private static final String NBTTAG_BLOCK_NE = "mGameBlockNE";
    private static final String NBTTAG_BLOCK_SW = "mGameBlockSW";
    private static final String NBTTAG_BLOCK_SE = "mGameBlockSE";
    private static final String NBTTAG_BLOCK_MASTER = "mMasterBlock";
    private static final String NBTTAG_BLOCKPOS_X = "posX";
    private static final String NBTTAG_BLOCKPOS_Y = "posY";
    private static final String NBTTAG_BLOCKPOS_Z = "posZ";
    private Vec3 mNorthPos;
    private Vec3 mSouthPos;
    private Vec3 mWestPos;
    private Vec3 mEastPos;
    private Vec3 mNorthWestPos;
    private Vec3 mNorthEastPos;
    private Vec3 mSouthWestPos;
    private Vec3 mSouthEastPos;
    private Vec3 mMasterPos;
    private LegacyGameOfLightTile _mGameBlockNorth;
    private LegacyGameOfLightTile _mGameBlockWest;
    private LegacyGameOfLightTile _mGameBlockSouth;
    private LegacyGameOfLightTile _mGameBlockEast;
    private LegacyGameOfLightTile _mGameBlockNE;
    private LegacyGameOfLightTile _mGameBlockNW;
    private LegacyGameOfLightTile _mGameBlockSE;
    private LegacyGameOfLightTile _mGameBlockSW;
    private LegacyGameOfLightTile _mMasterTE;
    private ExtendedDirections mTEDirection = ExtendedDirections.UP;
    private boolean mIsMaster;
    private boolean mIsActive;

    public boolean getIsActive() {
        return mIsActive;
    }

    public ExtendedDirections getDirection() {
        return mTEDirection;
    }

    private LegacyGameOfLightTile getBlockNorth() {
        if (_mGameBlockNorth == null && mNorthPos != null)
            _mGameBlockNorth = (LegacyGameOfLightTile) worldObj.getTileEntity((int) mNorthPos.xCoord, (int) mNorthPos.yCoord, (int) mNorthPos.zCoord);

        return _mGameBlockNorth;
    }

    private LegacyGameOfLightTile getBlockWest() {
        if (_mGameBlockWest == null && mWestPos != null)
            _mGameBlockWest = (LegacyGameOfLightTile) worldObj.getTileEntity((int) mWestPos.xCoord, (int) mWestPos.yCoord, (int) mWestPos.zCoord);

        return _mGameBlockWest;
    }

    private LegacyGameOfLightTile getBlockSouth() {
        if (_mGameBlockSouth == null && mSouthPos != null)
            _mGameBlockSouth = (LegacyGameOfLightTile) worldObj.getTileEntity((int) mSouthPos.xCoord, (int) mSouthPos.yCoord, (int) mSouthPos.zCoord);

        return _mGameBlockSouth;
    }

    private LegacyGameOfLightTile getBlockEast() {
        if (_mGameBlockEast == null && mEastPos != null)
            _mGameBlockEast = (LegacyGameOfLightTile) worldObj.getTileEntity((int) mEastPos.xCoord, (int) mEastPos.yCoord, (int) mEastPos.zCoord);

        return _mGameBlockEast;
    }

    private LegacyGameOfLightTile getBlockMaster() {
        if (_mMasterTE == null && mMasterPos != null)
            _mMasterTE = (LegacyGameOfLightTile) worldObj.getTileEntity((int) mMasterPos.xCoord, (int) mMasterPos.yCoord, (int) mMasterPos.zCoord);

        return _mMasterTE;
    }

    private LegacyGameOfLightTile getBlockNW() {
        if (_mGameBlockNW == null && mNorthWestPos != null)
            _mGameBlockNW = (LegacyGameOfLightTile) worldObj.getTileEntity((int) mNorthWestPos.xCoord, (int) mNorthWestPos.yCoord, (int) mNorthWestPos.zCoord);

        return _mGameBlockNW;
    }

    private LegacyGameOfLightTile getBlockNE() {
        if (_mGameBlockNE == null && mNorthEastPos != null)
            _mGameBlockNE = (LegacyGameOfLightTile) worldObj.getTileEntity((int) mNorthEastPos.xCoord, (int) mNorthEastPos.yCoord, (int) mNorthEastPos.zCoord);

        return _mGameBlockNE;
    }

    private LegacyGameOfLightTile getBlockSW() {
        if (_mGameBlockSW == null && mSouthWestPos != null)
            _mGameBlockSW = (LegacyGameOfLightTile) worldObj.getTileEntity((int) mSouthWestPos.xCoord, (int) mSouthWestPos.yCoord, (int) mSouthWestPos.zCoord);

        return _mGameBlockSW;
    }

    private LegacyGameOfLightTile getBlockSE() {
        if (_mGameBlockSE == null && mSouthEastPos != null)
            _mGameBlockSE = (LegacyGameOfLightTile) worldObj.getTileEntity((int) mSouthEastPos.xCoord, (int) mSouthEastPos.yCoord, (int) mSouthEastPos.zCoord);

        return _mGameBlockSE;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
        worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
    }

    private Vec3 readGameBlockFromNBT(NBTTagCompound pTag, String tTagName) {
        NBTTagCompound tSubTag = pTag.getCompoundTag(tTagName);

        int x = tSubTag.getInteger(NBTTAG_BLOCKPOS_X);
        int y = tSubTag.getInteger(NBTTAG_BLOCKPOS_Y);
        int z = tSubTag.getInteger(NBTTAG_BLOCKPOS_Z);

        return Vec3.createVectorHelper(x, y, z);
    }

    private void addGameBlockToNBT(LegacyGameOfLightTile pTargetObject, NBTTagCompound pTag, String tTagName) {
        NBTTagCompound tSubTag = new NBTTagCompound();

        tSubTag.setInteger(NBTTAG_BLOCKPOS_X, pTargetObject.xCoord);
        tSubTag.setInteger(NBTTAG_BLOCKPOS_Y, pTargetObject.yCoord);
        tSubTag.setInteger(NBTTAG_BLOCKPOS_Z, pTargetObject.zCoord);

        pTag.setTag(tTagName, tSubTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound pNBTTagCompound) {
        super.readFromNBT(pNBTTagCompound);

        mIsMaster = pNBTTagCompound.getBoolean(NBTTAG_ISMASTER);
        mIsActive = pNBTTagCompound.getBoolean(NBTTAG_ISACTIVE);
        mTEDirection = ExtendedDirections.VALID_DIRECTIONS[pNBTTagCompound.getInteger(NBTTAG_DIRECTION)];

        if (pNBTTagCompound.hasKey(NBTTAG_BLOCK_NORTH))
            mNorthPos = readGameBlockFromNBT(pNBTTagCompound, NBTTAG_BLOCK_NORTH);
        if (pNBTTagCompound.hasKey(NBTTAG_BLOCK_SOUTH))
            mSouthPos = readGameBlockFromNBT(pNBTTagCompound, NBTTAG_BLOCK_SOUTH);
        if (pNBTTagCompound.hasKey(NBTTAG_BLOCK_WEST))
            mWestPos = readGameBlockFromNBT(pNBTTagCompound, NBTTAG_BLOCK_WEST);
        if (pNBTTagCompound.hasKey(NBTTAG_BLOCK_EAST))
            mEastPos = readGameBlockFromNBT(pNBTTagCompound, NBTTAG_BLOCK_EAST);

        if (pNBTTagCompound.hasKey(NBTTAG_BLOCK_NE))
            mNorthEastPos = readGameBlockFromNBT(pNBTTagCompound, NBTTAG_BLOCK_NE);
        if (pNBTTagCompound.hasKey(NBTTAG_BLOCK_NW))
            mNorthWestPos = readGameBlockFromNBT(pNBTTagCompound, NBTTAG_BLOCK_NW);
        if (pNBTTagCompound.hasKey(NBTTAG_BLOCK_SW))
            mSouthWestPos = readGameBlockFromNBT(pNBTTagCompound, NBTTAG_BLOCK_SW);
        if (pNBTTagCompound.hasKey(NBTTAG_BLOCK_SE))
            mSouthEastPos = readGameBlockFromNBT(pNBTTagCompound, NBTTAG_BLOCK_SE);

        if (pNBTTagCompound.hasKey(NBTTAG_BLOCK_MASTER))
            mMasterPos = readGameBlockFromNBT(pNBTTagCompound, NBTTAG_BLOCK_MASTER);
    }

    @Override
    public void writeToNBT(NBTTagCompound pNBTTagCompound) {
        super.writeToNBT(pNBTTagCompound);

        pNBTTagCompound.setBoolean(NBTTAG_ISMASTER, mIsMaster);
        pNBTTagCompound.setBoolean(NBTTAG_ISACTIVE, mIsActive);
        pNBTTagCompound.setInteger(NBTTAG_DIRECTION, mTEDirection.ordinal());

        if (getBlockNorth() != null)
            addGameBlockToNBT(getBlockNorth(), pNBTTagCompound, NBTTAG_BLOCK_NORTH);
        if (getBlockSouth() != null)
            addGameBlockToNBT(getBlockSouth(), pNBTTagCompound, NBTTAG_BLOCK_SOUTH);
        if (getBlockWest() != null)
            addGameBlockToNBT(getBlockWest(), pNBTTagCompound, NBTTAG_BLOCK_WEST);
        if (getBlockEast() != null)
            addGameBlockToNBT(getBlockEast(), pNBTTagCompound, NBTTAG_BLOCK_EAST);

        if (getBlockNW() != null)
            addGameBlockToNBT(getBlockNW(), pNBTTagCompound, NBTTAG_BLOCK_NW);
        if (getBlockNE() != null)
            addGameBlockToNBT(getBlockNE(), pNBTTagCompound, NBTTAG_BLOCK_NE);
        if (getBlockSW() != null)
            addGameBlockToNBT(getBlockSW(), pNBTTagCompound, NBTTAG_BLOCK_SW);
        if (getBlockSE() != null)
            addGameBlockToNBT(getBlockSE(), pNBTTagCompound, NBTTAG_BLOCK_SE);

        if (getBlockMaster() != null)
            addGameBlockToNBT(getBlockMaster(), pNBTTagCompound, NBTTAG_BLOCK_MASTER);
    }

    @Override
    public void updateEntity() {
        if (!worldObj.isRemote) {
            ExtendedDirections direction = getDirection();
            BlockPos pos = BlockPos.of(xCoord, yCoord, zCoord);
            if (isHorizontal(direction)) {
                WorldExt.setBlock(worldObj, pos, LGBlocks.DUNGEON_WALL, DungeonBrick.Type.FLOOR_SHIELDED.ordinal(), 3);
            } else if (direction == UP) {
                WorldExt.setBlock(worldObj, pos, LGBlocks.DUNGEON_WALL, DungeonBrick.Type.FLOOR_SHIELDED.ordinal(), 3);

                BlockPos puzzle = pos.offset(0, 2, 0);
                if (WorldExt.getBlock(worldObj, puzzle).isReplaceable(worldObj, puzzle.getX(), puzzle.getY(), puzzle.getZ())) {
                    LegacyMigrator.LOGGER.debug("Found old Game Of Light block on {}! Converting structure back to puzzle master!", pos);
                    WorldExt.setBlock(worldObj, puzzle, LGBlocks.PUZZLE_MASTER);
                } else {
                    LegacyMigrator.LOGGER.debug("Found old Game Of Light block on {}! But the position for puzzle master is obstructed, so I just delete this minigame block, sorry :c", pos);
                }
            }
        }
    }
}
