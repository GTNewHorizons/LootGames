package eu.usrv.legacylootgames.config;


import eu.usrv.yamcore.auxiliary.IntHelper;
import eu.usrv.yamcore.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import ru.timeconqueror.lootgames.LegacyMigrator;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;


public class LegacyLGConfig extends ConfigManager {
    public boolean RetroGenDungeons; //ignored
    public boolean WorldGenEnabled;
    public String DungeonLoggerLogLevel;
    public boolean MinigamesEnabled;
    public GOLConfig GolConfig;
    public HashMap<Integer, Integer> DimensionWhitelist = new HashMap<>();

    public LegacyLGConfig(File pConfigBaseDirectory, String pModCollectionDirectory, String pModID) {
        super(pConfigBaseDirectory, pModCollectionDirectory, pModID);
    }

    private void parseDimensionConfig(String[] pDimensionList) {
        DimensionWhitelist = new HashMap<>();
        for (String tEntry : pDimensionList) {
            // Skip Zero-Length entries
            if (tEntry.length() == 0)
                return;

            String[] tArray = tEntry.split(";");
            if (tArray.length == 2) {
                if (IntHelper.tryParse(tArray[0]) && IntHelper.tryParse(tArray[1])) {
                    Integer tDimID = Integer.parseInt(tArray[0]);
                    Integer tRhombSize = Integer.parseInt(tArray[1]);

                    if (tRhombSize < 5 || tRhombSize > 100)
                        LegacyMigrator.LOGGER.error(String.format("Invalid DimensionWhitelist entry found: [%s;]; RhombusSize must be between 5 and 100", tEntry));
                    else {
                        if (!DimensionWhitelist.containsKey(tDimID)) {
                            DimensionWhitelist.put(tDimID, tRhombSize);
                        } else
                            LegacyMigrator.LOGGER.error(String.format("Invalid DimensionWhitelist entry found: [%s;]; DimensionID is already defined", tEntry));
                    }
                } else
                    LegacyMigrator.LOGGER.error(String.format("Invalid DimensionWhitelist entry found: [%s;]; DimensionID or Rhombus Size is not an Integer", tEntry));
            } else
                LegacyMigrator.LOGGER.error(String.format("Invalid DimensionWhitelist entry found: [%s;]; Syntax is <DimensionID>;<Rhombus Size>", tEntry));
        }
    }

    @Override
    protected void PreInit() {
        GolConfig = new GOLConfig(_mainConfig);
        WorldGenEnabled = false;

        DungeonLoggerLogLevel = Level.INFO.toString();
        RetroGenDungeons = false;
    }

    @Override
    protected void Init() {
        GolConfig.Init();
        MinigamesEnabled = _mainConfig.getBoolean("MinigamesEnabled", "main", MinigamesEnabled, "Switch to enable or disable the Master-Blocks. If disabled, no minigames will spawn. You can change this ingame");
        RetroGenDungeons = _mainConfig.getBoolean("RetroGenDungeons", "worldgen", RetroGenDungeons, "Enable or disable RetroGen");
        WorldGenEnabled = _mainConfig.getBoolean("WorldGenEnabled", "worldgen", WorldGenEnabled, "Enable or disable WorldGen");

        String[] tDimConfig = _mainConfig.getStringList("DimensionWhitelist", "worldgen", new String[]{"0; 20"}, "List DimensionIDs where LootGame Dungeons are allowed to spawn, with Rhombus Size. Syntax is <DimensionID>:<Rhombus Size>");
        parseDimensionConfig(tDimConfig);

        DungeonLoggerLogLevel = _mainConfig.getString("DungeonLoggerLogLevel", "debug", DungeonLoggerLogLevel, "LogLevel for the separate DungeonGenerator Logger. Valid options: info, debug, trace", new String[]{"INFO", "DEBUG", "TRACE"});
    }

    protected void PostInit() {
    }

    public static class GOLConfig {
        public LootStageConfig GameStageI;
        public LootStageConfig GameStageII;
        public LootStageConfig GameStageIII;
        public LootStageConfig GameStageIV;

        public int StartDigits;
        public int MaxGameTries;
        //public int MaxDigits;
        public int ExpandPlayFieldAtStage;
        public int Timeout;
        public boolean Debug;

        public boolean GameFail_Explode;
        public boolean GameFail_Spawn;
        public boolean GameFail_Lava;

        private final Configuration _mMainConfig;

        public GOLConfig(Configuration pMainConfig) {
            _mMainConfig = pMainConfig;

            GameStageI = new LootStageConfig(1);
            GameStageII = new LootStageConfig(2);
            GameStageIII = new LootStageConfig(3);
            GameStageIV = new LootStageConfig(4);

            GameStageI.LootTable = "dungeonChest";
            GameStageI.MinItems = 2;
            GameStageI.MaxItems = 2;
            GameStageI.MinDigitsRequired = 5;
            GameStageI.DisplayTime = 1200;
            GameStageI.RandomizeSequence = false;

            GameStageII.LootTable = "mineshaftCorridor";
            GameStageII.MinItems = 4;
            GameStageII.MaxItems = 4;
            GameStageII.MinDigitsRequired = 10;
            GameStageII.DisplayTime = 800;
            GameStageII.RandomizeSequence = false;

            GameStageIII.LootTable = "pyramidJungleChest";
            GameStageIII.MinItems = 6;
            GameStageIII.MaxItems = 6;
            GameStageIII.MinDigitsRequired = 15;
            GameStageIII.DisplayTime = 600;
            GameStageIII.RandomizeSequence = false;

            GameStageIV.LootTable = "strongholdCorridor";
            GameStageIV.MinItems = 8;
            GameStageIV.MaxItems = 8;
            GameStageIV.MinDigitsRequired = 20;
            GameStageIV.DisplayTime = 500;
            GameStageIV.RandomizeSequence = true;

            StartDigits = 2;
            MaxGameTries = 3;
            ExpandPlayFieldAtStage = 2;
            //MaxDigits = 25;
            GameFail_Explode = true;
            GameFail_Spawn = true;
            GameFail_Lava = true;
            Timeout = 60;
        }

        protected void Init() {
            loadStage(_mMainConfig, GameStageI, "StageI");
            loadStage(_mMainConfig, GameStageII, "StageII");
            loadStage(_mMainConfig, GameStageIII, "StageIII");
            loadStage(_mMainConfig, GameStageIV, "StageIV");

            StartDigits = _mMainConfig.getInt("StartDigits", "games.gol", StartDigits, 1, 256, "How many digits should be randomly choosen at game-start?");
            //MaxDigits = _mMainConfig.getInt( "MaxDigits", "games.gol", MaxDigits, -1, 256, "How many Digits in total should the game accept until it auto-stops? Set to -1 to continue infinite" );
            MaxGameTries = _mMainConfig.getInt("MaxGameTries", "games.gol", MaxGameTries, 1, 256, "How many attempts does a player have? 1 means the struct will fail after the first misclicked block");
            ExpandPlayFieldAtStage = _mMainConfig.getInt("ExpandPlayFieldAtStage", "games.gol", ExpandPlayFieldAtStage, 0, 4, "At which stage should the playfield become a full 3x3 pattern? Set 0 to disable and keep the 4-block size; set 1 to always start with 3x3");
            Timeout = _mMainConfig.getInt("Timeout", "games.gol", Timeout, 5, 600, "How long does it take to timeout a game? Value is in seconds. If no player input is done in that time, the game will go to sleep. The next player will start fresh");
            Debug = _mMainConfig.getBoolean("Debug", "games.gol", Debug, "Enable or disable Debugging of this game (Only enable this if you expect a bug. This will blow up your logfile...)");

            GameFail_Explode = _mMainConfig.getBoolean("ExplodeEvent", "games", GameFail_Explode, "Enable or disable struct exploding on max failed attempts");
            GameFail_Spawn = _mMainConfig.getBoolean("MobEvent", "games", GameFail_Spawn, "Enable or disable struct filling with monsters on max failed attempts");
            GameFail_Lava = _mMainConfig.getBoolean("LavaEvent", "games", GameFail_Lava, "Enable or disable struct filling with lava on max failed attempts");
        }

        private void loadStage(Configuration pConfig, LootStageConfig pConfObject, String pSection) {
            pConfObject.LootTable = pConfig.getString("LootTable", String.format("games.gol.gamestages.%s", pSection), pConfObject.LootTable, "The loottable for the chest in this stage");
            pConfObject.MinItems = pConfig.getInt("MinItems", String.format("games.gol.gamestages.%s", pSection), pConfObject.MinItems, 1, 256, "Minimum amount of items to be spawned");
            pConfObject.MaxItems = pConfig.getInt("MaxItems", String.format("games.gol.gamestages.%s", pSection), pConfObject.MaxItems, 1, 256, "Maximum amount of items to be spawned");
            pConfObject.MinDigitsRequired = pConfig.getInt("MinDigitsRequired", String.format("games.gol.gamestages.%s", pSection), pConfObject.MinDigitsRequired, 1, 256, "Minimum correct digits required to complete this stage and unlock the chest. This can be adjusted per-Dimension in S:DimensionalConfig");
            pConfObject.DisplayTime = pConfig.getInt("DisplayTime", String.format("games.gol.gamestages.%s", pSection), pConfObject.DisplayTime, 100, 2000, "The amount of time (in milliseconds; 1000ms = 1s) to wait at playback before moving to the next color");
            pConfObject.RandomizeSequence = pConfig.getBoolean("RandomizeSequence", String.format("games.gol.gamestages.%s", pSection), pConfObject.RandomizeSequence, "If true, the pattern will randomize on each level in this stage");
            String[] tDimConfig = pConfig.getStringList("DimensionalConfig", String.format("games.gol.gamestages.%s", pSection), new String[]{""}, "Syntax: <DimensionID>;<LootTableName>;<AdditionalDigitsRequired>; one line for each Dimension. If you use AdditionalDigitsRequired, make sure to use the same or an higher number on each stage");
            if (tDimConfig.length > 0)
                pConfObject.SetDimensionalLootConfig(tDimConfig);
        }
    }

    public static class LootStageConfig {
        public int MinItems;
        public int MaxItems;
        public int DisplayTime;
        public boolean RandomizeSequence;
        public int LevelID;
        public HashMap<Integer, DimensionalConfig> DimensionalLoots;
        public int MinDigitsRequired;
        public String LootTable;

        LootStageConfig(int pLevel) {
            DimensionalLoots = null;
            LevelID = pLevel;
        }

        public void SetDimensionalLootConfig(String[] pConfigList) {
            DimensionalLoots = new HashMap<>();
            for (String tEntry : pConfigList) {
                // Skip Zero-Length entries
                if (tEntry.length() == 0)
                    return;

                String[] tArray = tEntry.split(";");
                if (tArray.length == 3) {
                    if (IntHelper.tryParse(tArray[0]) && IntHelper.tryParse(tArray[2])) {
                        Integer tDimID = Integer.parseInt(tArray[0]);
                        Integer tAddDig = Integer.parseInt(tArray[2]);

                        if (!DimensionalLoots.containsKey(tDimID))
                            DimensionalLoots.put(tDimID, new DimensionalConfig(tArray[1], tAddDig));
                        else
                            LegacyMigrator.LOGGER.error(String.format("Invalid DimensionalLootConfig entry found: [%s;] DimensionID is already defined", tEntry));
                    } else
                        LegacyMigrator.LOGGER.error(String.format("Invalid DimensionalLootConfig entry found: [%s;]; DimensionID is not an Integer", tEntry));
                } else
                    LegacyMigrator.LOGGER.error(String.format("Invalid DimensionalLootConfig entry found: [%s;]; Syntax is <DimensionID>;<LootTableName>;<AdditionalDigitsRequired> ", tEntry));
            }

            LegacyMigrator.LOGGER.info(String.format("Loaded %d DimensionID based LootTables for StageID %d", DimensionalLoots.size(), LevelID));
        }

        public static class DimensionalConfig {
            public String LootTable;
            public int AdditionalDigits;

            DimensionalConfig(String pLootTable, int pAddDigits) {
                LootTable = pLootTable;
                AdditionalDigits = pAddDigits;
            }
        }
    }
}
