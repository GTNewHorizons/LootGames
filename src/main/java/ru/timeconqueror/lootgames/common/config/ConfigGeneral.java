package ru.timeconqueror.lootgames.common.config;

import net.minecraftforge.common.config.Configuration;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.Marker;
import ru.timeconqueror.timecore.api.common.config.Config;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ConfigGeneral extends Config {

    public boolean disableMinigames;
    public Set<Marker> enabledMarkers;
    public WorldGenCategory worldGen = new WorldGenCategory();

    public ConfigGeneral() {
        super("general");
    }

    @Override
    public void init() {
        disableMinigames = config.getBoolean(Names.DISABLE_MINIGAMES, Names.CATEGORY_MAIN, false, "If this is set to true, then puzzle master won't start any new game. Won't affect already started games.");
        String[] markers = Arrays.stream(Marker.values()).map(Enum::name).toArray(String[]::new);
        String[] enabledMarkers = config.getStringList("debug_markers", Names.CATEGORY_MAIN, new String[0], "Markers which enable extra console output.\nAvailable:\n" + String.join("\n", markers) + "\n", markers);
        this.enabledMarkers = Arrays.stream(enabledMarkers).map(Marker::byName).filter(Objects::nonNull).collect(Collectors.toSet());
        worldGen.init(config);
    }

    public static class Names {
        public static final String CATEGORY_MAIN = "main";
        public static final String CATEGORY_WORLDGEN = "worldgen";

        public static final String DISABLE_DUNGEON_GEN = "disable_dungeon_gen";
        public static final String DUNGEON_LOG_LEVEL = "dungeon_log_level";
        public static final String DISABLE_MINIGAMES = "disable_minigames";
        public static final String PER_DIMENSION_CONFIGS = "per_dimension_configs";
    }

    @Override
    public String getRelativePath() {
        return LGConfigs.resolve(getKey());
    }

    public static class WorldGenCategory {
        public boolean disableDungeonGen;
        public boolean retroGenDungeons;
        private Map<Integer, Integer> dimRhombs;
        public String dungeonLogLevel;

        private void init(Configuration config) {
            disableDungeonGen = config.getBoolean(Names.DISABLE_DUNGEON_GEN, Names.CATEGORY_WORLDGEN, false, "Enable or disable dungeon generation");
//            retroGenDungeons = config.getBoolean("retrogen_dungeons", "worldgen", false, "Enable or disable RetroGen"); is disabled
            String[] dimConfigs = config.getStringList(Names.PER_DIMENSION_CONFIGS, Names.CATEGORY_WORLDGEN, new String[]{"0| 20"}, "Whitelisted dimensions' ids that were allowed for dungeon generation and rhomb size.\nRhomb size means the size of rhombs, which will imaginary cover the world. Dungeon will be generated in each rhomb. \nSo the larger the size, the less chance of generation. \nRhomb size must be between 5 and 100. \nExample of array element: 0| 20 - this means that dungeons will be generated in rhombs with size equal to 20 in the overworld (ID = 0).");
            parseDimAndRhombList(dimConfigs);
            dungeonLogLevel = config.getString(Names.DUNGEON_LOG_LEVEL, Names.CATEGORY_WORLDGEN, Level.INFO.toString(), "Log level for the separate DungeonGenerator Logger. Valid options: INFO, DEBUG, TRACE", new String[]{"INFO", "DEBUG", "TRACE"});

            config.setCategoryComment(Names.CATEGORY_WORLDGEN, "Regulates dungeon appearing in world.");
        }

        private void parseDimAndRhombList(String[] dimAndRhombList) {
            dimRhombs = new HashMap<>();

            for (String entry : dimAndRhombList) {
                if (entry.length() == 0)
                    return;

                String[] arr = entry.split("\\|");
                if (arr.length != 2) {
                    LootGames.LOGGER.error("Invalid dimension rhomb entry found: {}. Syntax is <dimensionID>|<rhomb size>. This entry will be skipped.", entry);
                }

                try {
                    int dimID = Integer.parseInt(arr[0].trim());
                    int rhombSize = Integer.parseInt(arr[1].trim());

                    if (rhombSize < 5 || rhombSize > 100) {
                        LootGames.LOGGER.error("Invalid dimension rhomb entry found: {}. Rhomb size must be between 5 and 100.", entry);
                    } else {
                        if (!dimRhombs.containsKey(dimID)) {
                            dimRhombs.put(dimID, rhombSize);
                            LootGames.LOGGER.info("Worldgen enabled in dimension {} with rhomb size {}.", dimID, rhombSize);
                        } else
                            LootGames.LOGGER.error("Invalid dimension rhomb entry found: {}. DimensionID is already defined.", entry);
                    }
                } catch (NumberFormatException e) {
                    LootGames.LOGGER.error("Invalid dimension rhomb entry found: {}. DimensionID or Rhomb size is not an Integer. This entry will be skipped.", entry);
                }
            }
        }

        public boolean isDimensionEnabledForWG(int dimensionId) {
            return dimRhombs.containsKey(dimensionId);
        }

        public int getWorldGenRhombusSize(int dimensionId) {
            if (!isDimensionEnabledForWG(dimensionId))
                return -1;
            else
                return dimRhombs.get(dimensionId);
        }
    }
}
