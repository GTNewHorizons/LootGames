package eu.usrv.legacylootgames;


import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import eu.usrv.legacylootgames.blocks.DungeonBrick;
import eu.usrv.legacylootgames.blocks.DungeonLightSource;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.timecore.api.util.RandHelper;


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

    private final Material[] _mValidMaterials = {Material.wood, Material.water, Material.cactus, Material.snow, Material.grass, Material.leaves, Material.plants, Material.air, Material.lava, Material.portal};

    public static int getRoomWidth() {
        return PUZZLEROOM_CENTER_TO_BORDER * 2 + 1;
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

        LootGamesLegacy.DungeonLogger.trace("StructureGenerator => isValidSurfaceBlock() Result is: %s", tState);

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
            LootGamesLegacy.DungeonLogger.trace("StructureGenerator => setSurfaceLevel(): Scanning y=%d", tSurfaceY);

            for (int axisX = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisX <= PUZZLEROOM_CENTER_TO_BORDER; axisX++) {
                for (int axisZ = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisZ <= PUZZLEROOM_CENTER_TO_BORDER; axisZ++) {
                    if (!isValidSurfaceBlock(_mWorldObj.getBlock(axisX + pLocationX, tSurfaceY, axisZ + pLocationZ))) {
                        LootGamesLegacy.DungeonLogger.trace("StructureGenerator => setSurfaceLevel(): y=%d has failed. Block is not valid: %s", tSurfaceY, GameRegistry.findUniqueIdentifierFor(_mWorldObj.getBlock(axisX, tSurfaceY, axisZ)).toString());
                        tYLevelCheckPassed = false;
                    }

                    if (!tYLevelCheckPassed)
                        break;
                }
                if (!tYLevelCheckPassed)
                    break;
            }

            if (tYLevelCheckPassed) {
                LootGamesLegacy.DungeonLogger.trace("StructureGenerator => setSurfaceLevel(): y=%d has passed", tSurfaceY);
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
        LootGamesLegacy.Profiler.AddTimeToList("WorldGen => generatePuzzleMicroDungeon()", tStop - tStart);

        return tResult;
    }

    public boolean doGenDungeon(World pWorldObj, int pLocationX, int pLocationZ) {
        LootGamesLegacy.DungeonLogger.trace("StructureGenerator => generatePuzzleMicroDungeon()");

        _mWorldObj = pWorldObj;
        _mCenterX = pLocationX;
        _mCenterZ = pLocationZ;

        if (!setSurfaceLevel(pLocationX, pLocationZ)) {
            LootGamesLegacy.DungeonLogger.debug("Can't spawn PuzzleDungeon. Y level not found");
            return false;
        }
        LootGamesLegacy.DungeonLogger.trace("StructureGenerator => generatePuzzleMicroDungeon() : Surface level found. Dungeon bottom at: y= %d", _mDungeonBottom);

        if (checkForFreeSpace()) {
            for (int axisX = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisX <= PUZZLEROOM_CENTER_TO_BORDER; axisX++) {
                for (int axisZ = (PUZZLEROOM_CENTER_TO_BORDER * -1); axisZ <= PUZZLEROOM_CENTER_TO_BORDER; axisZ++) {
                    for (int axisY = _mDungeonBottom; axisY <= _mDungeonTop; axisY++) {
                        // Clear the space first before we continue
                        _mWorldObj.setBlockToAir(axisX + _mCenterX, axisY, axisZ + _mCenterZ);

                        // Static center block with masterTE
                        if (axisX == 0 && axisZ == 0 && axisY == _mDungeonBottom + PUZZLEROOM_MASTER_TE_OFFSET)
                            _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LGBlocks.PUZZLE_MASTER);
                        else if (axisY == _mDungeonBottom) // bottom layer of the dungeon
                            _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LGBlocks.DUNGEON_WALL, RandHelper.chance(10, DungeonBrick.Type.FLOOR_CRACKED.ordinal(), DungeonBrick.Type.FLOOR.ordinal()), 2);
                        else if (axisY == _mDungeonTop) // Top layer of the dungeon
                            _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LGBlocks.DUNGEON_WALL, RandHelper.chance(10, DungeonBrick.Type.CEILING_CRACKED.ordinal(), DungeonBrick.Type.CEILING.ordinal()), 2);
                        else if (axisY == _mDungeonBottom + 1) // Playfield placeholder to the player doesn't stand within generated blocks
                            _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LGBlocks.DUNGEON_WALL, DungeonBrick.Type.FLOOR_SHIELDED.ordinal(), 2);
                        else {
                            if (axisX == (PUZZLEROOM_CENTER_TO_BORDER * -1) || axisX == PUZZLEROOM_CENTER_TO_BORDER || axisZ == (PUZZLEROOM_CENTER_TO_BORDER * -1) || axisZ == PUZZLEROOM_CENTER_TO_BORDER) {
                                if (axisY == (_mDungeonTop - (int) Math.floor((PUZZLEROOM_HEIGHT / 2))))
                                    _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LGBlocks.DUNGEON_LAMP, RandHelper.chance(10, DungeonLightSource.State.BROKEN.ordinal(), DungeonLightSource.State.NORMAL.ordinal()), 2);
                                else
                                    _mWorldObj.setBlock(axisX + _mCenterX, axisY, axisZ + _mCenterZ, LGBlocks.DUNGEON_WALL, RandHelper.chance(10, DungeonBrick.Type.WALL_CRACKED.ordinal(), DungeonBrick.Type.WALL.ordinal()), 2);
                            }
                        }
                    }
                }
            }
            LootGamesLegacy.DungeonLogger.info(String.format("PuzzleDungeon spawned at %d %d %d in Dimension %d", _mCenterX, _mDungeonBottom, _mCenterZ, pWorldObj.provider.dimensionId));

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
            LootGamesLegacy.DungeonLogger.debug(String.format("PuzzleDungeon not spawned, location at X/Z %d %d is not suitable", pLocationX, pLocationZ));
            return false;
        }

    }

    private boolean checkForFreeSpace() {
        boolean tResult = true;
        LootGamesLegacy.DungeonLogger.trace("StructureGenerator => checkForFreeSpace()");
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
                        LootGamesLegacy.DungeonLogger.debug(String.format("Block at %d %d %d is a TileEntitiy. Skipping dungeon generator", axisX + _mCenterX, axisY, axisZ + _mCenterZ));
                        tResult = false;
                        break;
                    }
                }
            }
        }
        LootGamesLegacy.DungeonLogger.trace("StructureGenerator => checkForFreeSpace() : Result is %s", tResult);
        return tResult;
    }
}
