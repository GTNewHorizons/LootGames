package eu.usrv.legacylootgames.worldgen;


import cpw.mods.fml.common.IWorldGenerator;
import eu.usrv.legacylootgames.LootGamesLegacy;
import eu.usrv.legacylootgames.StructureGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import ru.timeconqueror.lootgames.common.config.LGConfigs;

import java.util.Random;


public class LootGamesWorldGen implements IWorldGenerator {
    private final Random _mRnd;

    public LootGamesWorldGen() {
        _mRnd = new Random();
        LootGamesLegacy.DungeonLogger.debug("WorldGen => Init().");
    }

    @Override
    public void generate(Random pRandom, int pChunkX, int pChunkZ, World pWorld, IChunkProvider pChunkGenerator, IChunkProvider pChunkProvider) {
        long tStart = System.currentTimeMillis();
        LootGamesLegacy.DungeonLogger.trace("WorldGen => Generate()");
        if (!checkSpawnConditions(pChunkX, pChunkZ, pWorld)) {
            LootGamesLegacy.DungeonLogger.trace("Stopped this worldgen run");
            return;
        }
        StructureGenerator sGen = new StructureGenerator();

        sGen.generatePuzzleMicroDungeon(pWorld, (pChunkX << 4) + 8, (pChunkZ << 4) + 8);
        long tStop = System.currentTimeMillis();
        LootGamesLegacy.Profiler.AddTimeToList("WorldGen", tStop - tStart);
    }

    private boolean checkSpawnConditions(int pChunkX, int pChunkZ, World pWorld) {
        long tStart = System.currentTimeMillis();
        boolean tState = true;

        if (LGConfigs.GENERAL.worldGen.disableDungeonGen) {
            LootGamesLegacy.DungeonLogger.trace("WorldGen => Generate() => checkSpawnConditions() => WorldGen is DISABLED");
            tState = false;
        }

        if (tState && !LGConfigs.GENERAL.worldGen.isDimensionEnabledForWG(pWorld.provider.dimensionId)) {
            LootGamesLegacy.DungeonLogger.trace("WorldGen => Generate() => checkSpawnConditions() => Dim %d is not Whitelisted", pWorld.provider.dimensionId);
            tState = false;
        }

        if (tState && !canSpawnInChunk_v3(pChunkX, pChunkZ, pWorld)) {
            LootGamesLegacy.DungeonLogger.trace("WorldGen => Generate() => checkSpawnConditions() => Location not suitable");
            tState = false;
        }

        if (tState)
            LootGamesLegacy.DungeonLogger.trace("WorldGen => Generate() => canSpawnInChunk() => Location suitable");

        long tStop = System.currentTimeMillis();
        LootGamesLegacy.Profiler.AddTimeToList("WorldGen => checkSpawnConditions()", tStop - tStart);
        return tState;
    }

    private boolean canSpawnInChunk_v3(int pChunkX, int pChunkZ, World pWorld) {
        boolean tState = false;
        int tRhombSize = LGConfigs.GENERAL.worldGen.getWorldGenRhombusSize(pWorld.provider.dimensionId);

        int xc = (pChunkX * 2) + pChunkZ, zc = (pChunkZ * 2) + pChunkX;
        _mRnd.setSeed(pWorld.getSeed() + (xc / (tRhombSize * 2)) + ((zc / (tRhombSize * 2)) << 14));

        int pos1 = 3 + _mRnd.nextInt(tRhombSize * 2 - 3);
        int pos2 = 3 + _mRnd.nextInt(tRhombSize * 2 - 3);

        int tModXC = mod(xc, tRhombSize * 2);
        int tModZC = mod(zc, tRhombSize * 2);

        if (tModXC >= 3 && tModZC >= 3) {
            if ((tModXC == pos1 && tModZC == pos2) || (tModXC == pos1 + 1 && (tModZC == pos2 || tModZC == pos2 + 1)))
                tState = true;
        }

        return tState;
    }

    /**
     * Java, y u no do proper modulo?
     *
     * @param x
     * @param div
     * @return
     */
    int mod(int x, int div) {
        int r = x % div;
        return r < 0 ? r + div : r;
    }
}