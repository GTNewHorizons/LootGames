package ru.timeconqueror.lootgames.common.config;

import net.minecraftforge.common.config.Configuration;
import ru.timeconqueror.lootgames.LootGames;

public class LGConfigs {
    public static final ConfigGeneral GENERAL = new ConfigGeneral();
    public static final ConfigMS MINESWEEPER = new ConfigMS();
    public static final ConfigGOL GOL = new ConfigGOL();
    public static final ConfigRewards REWARDS = new ConfigRewards();

    public static String resolve(String path) {
        return LootGames.MODID + "/" + path + ".cfg";
    }

    public static String mergeCategories(String parent, String child) {
        return parent + Configuration.CATEGORY_SPLITTER + child;
    }

    public static void load() {
        GENERAL.load();
        MINESWEEPER.load();
        GOL.load();
        REWARDS.load();
    }
}