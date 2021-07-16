package ru.timeconqueror.lootgames.common.config.base;

import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.timecore.api.common.config.ConfigSection;

import java.util.HashMap;

public class RewardConfig extends ConfigSection {
    public int minItems;
    public int maxItems;
    public String defaultLootTable;

    private final HashMap<Integer, String> dimensionsConfigs = new HashMap<>();

    private final Defaults defaults;

    public RewardConfig(String parent, String key, String comment, Defaults defaults) {
        super(parent, key, comment);
        this.defaults = defaults;
    }

    public static class Names {
        public static final String MIN_ITEMS = "min_items";
        public static final String MAX_ITEMS = "max_items";
        public static final String DEFAULT_LOOT_TABLE = "default_loot_table";
        public static final String PER_DIM_CONFIGS = "per_dim_configs";
    }

    protected void init(Configuration config) {
        minItems = config.getInt(Names.MIN_ITEMS, getCategoryName(), defaults.minItems, 0, 256, "Minimum amount of item stacks to be generated in chest.");
        maxItems = config.getInt(Names.MAX_ITEMS, getCategoryName(), defaults.maxItems, 1, 256, "Maximum amount of item stacks to be generated in chest.");
        defaultLootTable = config.getString(Names.DEFAULT_LOOT_TABLE, getCategoryName(), defaults.lootTable, "Name of the loot table, items from which will be generated in the chest of this stage. This can be adjusted per dimension in \"per_dim_configs\".");

        String[] perDimConfigs = config.getStringList(Names.PER_DIM_CONFIGS, getCategoryName(), new String[]{}, "Here you can add different loot tables to each dimension. If dimension isn't in this list, then game will take default loot table for this stage.\nSyntax: <dimension_key>|<loottable_name>\n<loottable_name> - The loottable name for the chest in this stage.\nGeneral Example: [ \"0|minecraft:chests/simple_dungeon\" ]");
        parseDimConfigs(perDimConfigs);
    }

    public String getLootTable(World world) {
        String lootTable = dimensionsConfigs.get(world.provider.dimensionId);

        return lootTable != null ? lootTable : defaultLootTable;
    }

    private void parseDimConfigs(String[] perDimConfigs) {
        dimensionsConfigs.clear();

        for (String entry : perDimConfigs) {
            String[] config = entry.split("\\|");
            for (int i = 0; i < config.length; i++) {
                config[i] = config[i].trim();
            }

            if (config.length == 2) {
                int dimKey;
                try {
                    dimKey = Integer.parseInt(config[0]);
                } catch (NumberFormatException e) {
                    LootGames.LOGGER.error("Invalid dimension configs entry found: {}. Dimension id is not an Integer.  Skipping entry...", entry);
                    continue;
                }

                if (dimensionsConfigs.containsKey(dimKey)) {
                    LootGames.LOGGER.error("Invalid dimension configs entry found: {}. Dimension id is already defined. Skipping entry...", entry);
                    continue;
                }

                if (!config[1].isEmpty()) {
                    dimensionsConfigs.put(dimKey, config[1]);
                } else {
                    LootGames.LOGGER.error("Invalid dimension configs entry found: {}. LootTable key must not be an empty string. Skipping entry...", entry);
                }
            } else {
                LootGames.LOGGER.error("Invalid dimension configs entry found: {}. Syntax is <dimension_key>|<loottable_key>.  Skipping entry...", entry);
            }
        }
    }

    public static class Defaults {
        private final String lootTable;
        private final int minItems;
        private final int maxItems;

        public Defaults(String lootTable, int minItems, int maxItems) {
            this.lootTable = lootTable;
            this.minItems = minItems;
            this.maxItems = maxItems;
        }
    }
}
