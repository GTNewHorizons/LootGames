package eu.usrv.legacylootgames;

import java.io.IOException;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import eu.usrv.legacylootgames.auxiliary.ProfilingStorage;
import eu.usrv.legacylootgames.command.ProfilingCommand;
import eu.usrv.legacylootgames.gol.blocks.LegacyLightGameBlock;
import eu.usrv.legacylootgames.gol.tiles.LegacyGameOfLightTile;
import eu.usrv.legacylootgames.worldgen.LootGamesWorldGen;
import ru.timeconqueror.lootgames.LootGames;

public class LootGamesLegacy {
    public static LootGamesDungeonLogger DungeonLogger;
    public static ProfilingStorage Profiler;

    public static LootGamesWorldGen WorldGen;

    public static LegacyLightGameBlock legacyGolMaster = new LegacyLightGameBlock();

    public static void PreLoad(FMLPreInitializationEvent PreEvent) {
        GameRegistry.registerTileEntity(LegacyGameOfLightTile.class, "LOOTGAMES_GOL_TE");
        GameRegistry.registerBlock(legacyGolMaster, "GOLMasterBlock");

        Profiler = new ProfilingStorage();
        DungeonLogger = new LootGamesDungeonLogger();
        try {
            DungeonLogger.setup();
        } catch (IOException e) {
            LootGames.LOGGER.error("Unable to open DungeonLogger logfile. Spawned dungeons will not get recorded!");
            e.printStackTrace();
        }

        WorldGen = new LootGamesWorldGen();
        GameRegistry.registerWorldGenerator(WorldGen, Integer.MAX_VALUE);
    }

    public static void serverLoad(FMLServerStartingEvent pEvent) {
        pEvent.registerServerCommand(new ProfilingCommand());
    }
}
