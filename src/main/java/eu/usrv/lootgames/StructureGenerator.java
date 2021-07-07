package eu.usrv.lootgames;


import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import eu.usrv.lootgames.auxiliary.RandHelper;
import eu.usrv.lootgames.blocks.DungeonBrick;
import eu.usrv.lootgames.blocks.DungeonLightSource;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;


public class StructureGenerator {
    public static int PUZZLEROOM_CENTER_TO_BORDER = 10;
    public static int PUZZLEROOM_HEIGHT = 8;
    public static int PUZZLEROOM_SURFACE_DISTANCE = 2;
    public static int PUZZLEROOM_MASTER_TE_OFFSET = 3;

    private World _mWorldObj;
    private int _mCenterX;
    private int _mCenterZ;
    private int _mSurface = -1;
    private int _mDungeonTop = -1;
    private int _mDungeonBottom = -1;

    private Material[] _mValidMaterials = {Material.wood, Material.water, Material.cactus, Material.snow, Material.grass, Material.leaves, Material.plants, Material.air, Material.lava, Material.portal};

    public static void resetUnbreakablePlayfield(World pWorldObject, int pCoordX, int pCoordY, int pCoordZ) {
        for (int axisX = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisX <= PUZZLEROOM_CENTER_TO_BORDER; axisX++) {
            for (int axisZ = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisZ <= PUZZLEROOM_CENTER_TO_BORDER; axisZ++) {
                if (pWorldObject.getBlock(pCoordX + axisX, pCoordY, pCoordZ + axisZ) == LootGames.DungeonWallBlock) {
                    if (pWorldObject.getBlockMetadata(pCoordX + axisX, pCoordY, pCoordZ + axisZ) == 6)
                        pWorldObject.setBlockMetadataWithNotify(pCoordX + axisX, pCoordY, pCoordZ + axisZ, 2, 2);
                }
            }
        }
    }

    /**
     * Make sure the block is really a "ground" block, and no plants, trees, water, whatever
     *
     * @param pBlock
     * @return
     */
    private boolean isValidSurfaceBlock(Block pBlock) {
        boolean tState = true;

        for (Material m : _mValidMaterials) {
            if (pBlock.getMaterial() == m) {
                tState = false;
                break;
            }
        }

        LootGames.DungeonLogger.trace("StructureGenerator => isValidSurfaceBlock() Result is: %s", tState);

        return tState;
    }

    /**
     * Determine the surface level. This will scan the entire size the dungeon will use, in order to skip all "gaps" in
     * worldgen
     */
    private boolean setSurfaceLevel(int pLocationX, int pLocationZ) {
        boolean tYLevelCheckPassed = true;

        for (int tSurfaceY = 128; tSurfaceY > 20; tSurfaceY--) {
            tYLevelCheckPassed = true;
            LootGames.DungeonLogger.trace("StructureGenerator => setSurfaceLevel(): Scanning y=%d", tSurfaceY);

            for (int axisX = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisX <= PUZZLEROOM_CENTER_TO_BORDER; axisX++) {
                for (int axisZ = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisZ <= PUZZLEROOM_CENTER_TO_BORDER; axisZ++) {
                    if (!isValidSurfaceBlock(_mWorldObj.getBlock(axisX + pLocationX, tSurfaceY, axisZ + pLocationZ))) {
                        LootGames.DungeonLogger.trace("StructureGenerator => setSurfaceLevel(): y=%d has failed. Block is not valid: %s", tSurfaceY, GameRegistry.findUniqueIdentifierFor(_mWorldObj.getBlock(axisX, tSurfaceY, axisZ)).toString());
                        tYLevelCheckPassed = false;
                    }

                    if (!tYLevelCheckPassed)
                        break;
                }
                if (!tYLevelCheckPassed)
                    break;
            }

            if (tYLevelCheckPassed) {
                LootGames.DungeonLogger.trace("StructureGenerator => setSurfaceLevel(): y=%d has passed", tSurfaceY);
                _mSurface = tSurfaceY;
                _mDungeonTop = _mSurface - PUZZLEROOM_SURFACE_DISTANCE;
                _mDungeonBottom = _mSurface - (PUZZLEROOM_HEIGHT + PUZZLEROOM_SURFACE_DISTANCE);
                break;
            }
        }

        return tYLevelCheckPassed;
    }

    public boolean generatePuzzleMicroDungeon(World pWorldObj, int pLocationX, int pLocationZ) {
        long tStart = System.currentTimeMillis();
        boolean tResult = doGenDungeon(pWorldObj, pLocationX, pLocationZ);
        long tStop = System.currentTimeMillis();
        LootGames.Profiler.AddTimeToList("WorldGen => generatePuzzleMicroDungeon()", tStop - tStart);

        return tResult;
    }

    public boolean doGenDungeon(World pWorldObj, int pLocationX, int pLocationZ) {
        LootGames.DungeonLogger.trace("StructureGenerator => generatePuzzleMicroDungeon()");

        _mWorldObj = pWorldObj;
        _mCenterX = pLocationX;
        _mCenterZ = pLocationZ;

        if (!setSurfaceLevel(pLocationX, pLocationZ)) {
            LootGames.DungeonLogger.debug("Can't spawn PuzzleDungeon. Y level not found");
            return false;
        }
        LootGames.DungeonLogger.trace("StructureGenerator => generatePuzzleMicroDungeon() : Surface level found. Dungeon bottom at: y= %d", _mDungeonBottom);

        if (checkForFreeSpace()) {
            for (int axisX = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisX <= PUZZLEROOM_CENTER_TO_BORDER; axisX++) {
                for (int axisZ = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisZ <= PUZZLEROOM_CENTER_TO_BORDER; axisZ++) {
                    for (int axisY = _mDungeonBottom; axisY <= _mDungeonTop; axisY++) {
                        // Clear the space first before we continue
                        _mWorldObj.setBlockToAir(axisX + _mCenterX, axisY, axisZ + _mCenterZ);

                        // Static center block with masterTE
                        if (axisX == 0 && axisZ == 0 && axisY == _mDungeonBottom + PUZZLEROOM_MASTER_TE_OFFSET)
                            _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LootGames.MasterBlock);
                        else if (axisY == _mDungeonBottom) // bottom layer of the dungeon
                            _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LootGames.DungeonWallBlock, RandHelper.chance(10, DungeonBrick.eDungeonBricks.FLOOR_CRACKED.ordinal(), DungeonBrick.eDungeonBricks.FLOOR.ordinal()), 2);
                        else if (axisY == _mDungeonTop) // Top layer of the dungeon
                            _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LootGames.DungeonWallBlock, RandHelper.chance(10, DungeonBrick.eDungeonBricks.CEILING_CRACKED.ordinal(), DungeonBrick.eDungeonBricks.CEILING.ordinal()), 2);
                        else if (axisY == _mDungeonBottom + 1) // Playfield placeholder to the player doesn't stand within generated blocks
                            _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LootGames.DungeonWallBlock, DungeonBrick.eDungeonBricks.FLOOR_SHIELDED.ordinal(), 2);
                        else {
                            if (axisX == (PUZZLEROOM_CENTER_TO_BORDER * -1) || axisX == PUZZLEROOM_CENTER_TO_BORDER || axisZ == (PUZZLEROOM_CENTER_TO_BORDER * -1) || axisZ == PUZZLEROOM_CENTER_TO_BORDER) {
                                if (axisY == (_mDungeonTop - (int) Math.floor((PUZZLEROOM_HEIGHT / 2))))
                                    _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LootGames.DungeonLightBlock, RandHelper.chance(10, DungeonLightSource.eState.BROKEN.ordinal(), DungeonLightSource.eState.NORMAL.ordinal()), 2);
                                else
                                    _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LootGames.DungeonWallBlock, RandHelper.chance(10, DungeonBrick.eDungeonBricks.WALL_CRACKED.ordinal(), DungeonBrick.eDungeonBricks.WALL.ordinal()), 2);
                            }
                        }
                    }
                }
            }
            LootGames.DungeonLogger.info(String.format("PuzzleDungeon spawned at %d %d %d in Dimension %d", _mCenterX, _mDungeonBottom, _mCenterZ, pWorldObj.provider.dimensionId));

            // Generate entrance
            // Loop as long as the last "staircase" block is something different than air
            // and is obstructed. This might lead to a very long staircase on mountains...
            // So we limit the distance from the border to max 15 blocks
            // If room needs to be even, make the entrance 2 blocks wide
            Block tEntrance = Blocks.air;
            int axisZ = PUZZLEROOM_CENTER_TO_BORDER;
            int axisY = _mDungeonBottom + 2;
            int axisX = _mCenterX;
            do {
                axisZ++;

                for (axisX = _mCenterX; axisX <= _mCenterX; axisX++)
                    for (axisY = (_mDungeonBottom + 2); axisY <= (_mDungeonBottom + 4); axisY++) {
                        Block tCurrentBlock = _mWorldObj.getBlock(axisX, axisY + (axisZ - PUZZLEROOM_CENTER_TO_BORDER), axisZ + _mCenterZ);
                        if (tCurrentBlock.getMaterial().isSolid())
                            _mWorldObj.setBlockToAir(axisX, axisY + (axisZ - PUZZLEROOM_CENTER_TO_BORDER), axisZ + _mCenterZ);
                        else
                            break;
                    }

                tEntrance = _mWorldObj.getBlock(_mCenterX, axisY + (axisZ - PUZZLEROOM_CENTER_TO_BORDER), axisZ + _mCenterZ);

                if ((axisZ - PUZZLEROOM_CENTER_TO_BORDER) >= 15)
                    break;
            }
            while (tEntrance != Blocks.air || !_mWorldObj.canBlockSeeTheSky(_mCenterX, axisY + (axisZ - PUZZLEROOM_CENTER_TO_BORDER), axisZ + _mCenterZ));

            return true;

        } else {
            LootGames.DungeonLogger.debug(String.format("PuzzleDungeon not spawned, location at X/Z %d %d is not suitable", pLocationX, pLocationZ));
            return false;
        }

    }

    private boolean checkForFreeSpace() {
        boolean tResult = true;
        LootGames.DungeonLogger.trace("StructureGenerator => checkForFreeSpace()");
        for (int axisX = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisX <= PUZZLEROOM_CENTER_TO_BORDER; axisX++) {
            for (int axisZ = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisZ <= PUZZLEROOM_CENTER_TO_BORDER; axisZ++) {
                for (int axisY = _mDungeonBottom; axisY <= _mDungeonTop; axisY++) {
                    Block tBlock = _mWorldObj.getBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ);

                    // Skip TE check for GT Blockores
                    UniqueIdentifier tBlockID = GameRegistry.findUniqueIdentifierFor(tBlock);
                    if (tBlockID.modId.equalsIgnoreCase("gregtech") && tBlockID.name.toLowerCase().contains("blockores"))
                        continue;

                    TileEntity tTE = _mWorldObj.getTileEntity(axisX + _mCenterX, axisY, axisZ + _mCenterZ);
                    if (tTE != null) {
                        LootGames.DungeonLogger.debug(String.format("Block at %d %d %d is a TileEntitiy. Skipping dungeon generator", axisX + _mCenterX, axisY, axisZ + _mCenterZ));
                        tResult = false;
                        break;
                    }
                }
            }
        }
        LootGames.DungeonLogger.trace("StructureGenerator => checkForFreeSpace() : Result is %s", tResult);
        return tResult;
    }
}