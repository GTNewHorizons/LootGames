package eu.usrv.legacylootgames;


import net.minecraft.world.World;


public interface ILootGame {
    void init();

    /**
     * Executed upon structure-generation.
     * The entire game-room will be pushed to this function, so DO NOT run custom loops
     * here! Just either set a block, and return true, or skip it, and return false.
     * IMPORTANT: Make sure to not place anything directly on pBottomY when pOffsetY is 0,
     * unless you want to replace the mossy-cobblestone bottom of the struct
     *
     * @param pWorldObject The WorldObj for the Generator
     * @param pMaxXZ       The maximum X and Z offsets you will receive. If pOffsetZ == pMaxXZ, then you are on the the corner of the struct
     * @param pMaxY        Same as pMaxXZ. pOffsetY == pMaxY is the ceiling
     * @param pCenterX     The Center block of the structure on the X-Axis
     * @param pBottomY     The bottom layer of the structure
     * @param pCenterZ     The Center block of the structure on the Z-Axis
     * @param pOffsetX     The offset from pCenterX
     * @param pOffsetY     The offset from pBottomY
     * @param pOffsetZ     The offset from pCenterZ
     * @return TRUE, if you have set any block, FALSE, if you have not.
     */
    boolean onGenerateBlock(World pWorldObject, int pMaxXZ, int pMaxY, int pCenterX, int pBottomY, int pCenterZ, int pOffsetX, int pOffsetY, int pOffsetZ);
}
