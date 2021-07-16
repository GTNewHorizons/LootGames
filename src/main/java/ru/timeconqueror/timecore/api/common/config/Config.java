package ru.timeconqueror.timecore.api.common.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public abstract class Config {
    private static File configDir;
    protected final Configuration config;

    private final String key;

    public Config(String key) {
        this.key = key;
        File configFile = new File(configDir, getRelativePath());
        this.config = new Configuration(configFile);
    }

    public static void setConfigDir(File configDir) {
        Config.configDir = configDir;
    }

    /**
     * Returns the relative path to the file, where config will be saved and read.
     * <br>
     * Example:
     * {@code lootgames/minesweeper.cfg} will be saved in {@code config/lootgames/minesweeper.toml}.
     */
    public abstract String getRelativePath();

    public String getKey() {
        return key;
    }

    public abstract void init();

    public Configuration getConfig() {
        return config;
    }

    public void load() {
        config.load();
        init();
        config.save();
    }
}