package ru.timeconqueror.lootgames;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import eu.usrv.legacylootgames.config.LegacyLGConfig;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.timeconqueror.lootgames.common.config.ConfigGOL;
import ru.timeconqueror.lootgames.common.config.ConfigGeneral;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.common.config.base.RewardConfig;
import ru.timeconqueror.timecore.api.common.config.Config;

import java.io.File;
import java.io.IOException;

public class LegacyMigrator {
    public static final Logger LOGGER = LogManager.getLogger("LootGames Legacy Migrator");

    public static void onPreInit(FMLPreInitializationEvent event) {
        tryMigrateConfigs(event);
    }

    private static void tryMigrateConfigs(FMLPreInitializationEvent event) {
        File file = new File(event.getModConfigurationDirectory(), "LootGames/lootgames.cfg");
        if (file.exists()) {
            LOGGER.info("Detected legacy config file. Migrating...");
            LegacyLGConfig legacyCfg = new LegacyLGConfig(event.getModConfigurationDirectory(), LootGames.MODNAME, LootGames.MODID);
            if (!legacyCfg.LoadConfig()) {
                try {
                    FileUtils.copyFile(file, new File(file.getParent(), "lootgames.cfg_old"), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                throw new RuntimeException("LootGames couldn't load legacy config from '{}'. Please make changes in new configs manually! We added '_old' postfix to the file extension to not trigger migrator again.");
            }

            migrateConfigs(legacyCfg);

            boolean cantDelete;
            try {
                cantDelete = !file.delete();
            } catch (Exception e) {
                cantDelete = true;
                e.printStackTrace();
            }

            if (cantDelete) {
                throw new RuntimeException("Configs were migrated, but we couldn't delete old file '" + file.getAbsolutePath() + "', please delete it manually and then launch the game again!");
            }
        }
    }

    private static void migrateConfigs(LegacyLGConfig legacyCfg) {
        Configuration cfgGeneral = LGConfigs.GENERAL.getConfig();
        ConfigCategory catWorldGen = cfgGeneral.getCategory(ConfigGeneral.Names.CATEGORY_WORLDGEN);
        catWorldGen.get(ConfigGeneral.Names.DISABLE_DUNGEON_GEN).set(!legacyCfg.WorldGenEnabled);
        catWorldGen.get(ConfigGeneral.Names.DUNGEON_LOG_LEVEL).set(legacyCfg.DungeonLoggerLogLevel);
        catWorldGen.get(ConfigGeneral.Names.PER_DIMENSION_CONFIGS).set(legacyCfg.DimensionWhitelist.entrySet().stream().map(e -> e.getKey() + "|" + e.getValue()).toArray(String[]::new));

        ConfigCategory catMain = cfgGeneral.getCategory(ConfigGeneral.Names.CATEGORY_MAIN);
        catMain.get(ConfigGeneral.Names.DISABLE_MINIGAMES).set(!legacyCfg.MinigamesEnabled);

        LegacyLGConfig.GOLConfig legacyGol = legacyCfg.GolConfig;

        Configuration cfgGol = LGConfigs.GOL.getConfig();
        ConfigCategory catGol = cfgGol.getCategory(ConfigGOL.Names.CATEGORY_GAME_OF_LIGHT);
        catGol.get(ConfigGOL.Names.START_DIGIT_AMOUNT).set(legacyGol.StartDigits);
        catGol.get(ConfigGOL.Names.ATTEMPT_COUNT).set(legacyGol.MaxGameTries);
        catGol.get(ConfigGOL.Names.EXPAND_FIELD_AT_STAGE).set(legacyGol.ExpandPlayFieldAtStage);
        catGol.get(ConfigGOL.Names.TIMEOUT).set(legacyGol.Timeout);
        catGol.get(ConfigGOL.Names.EXPLODE_ON_FAIL).set(legacyGol.GameFail_Explode);
        catGol.get(ConfigGOL.Names.ZOMBIES_ON_FAIL).set(legacyGol.GameFail_Spawn);
        catGol.get(ConfigGOL.Names.LAVA_ON_FAIL).set(legacyGol.GameFail_Lava);

        int rounds = legacyGol.StartDigits;
        Configuration cfgRewards = LGConfigs.REWARDS.getConfig();
        rounds = processStageConfig(rounds, legacyGol.GameStageI, cfgGol.getCategory(LGConfigs.GOL.stage1.getCategoryName()), cfgRewards.getCategory(LGConfigs.REWARDS.rewardsGol.getStage1().getCategoryName()), cfgRewards.getCategory(LGConfigs.REWARDS.rewardsMinesweeper.getStage1().getCategoryName()));
        rounds = processStageConfig(rounds, legacyGol.GameStageII, cfgGol.getCategory(LGConfigs.GOL.stage2.getCategoryName()), cfgRewards.getCategory(LGConfigs.REWARDS.rewardsGol.getStage2().getCategoryName()), cfgRewards.getCategory(LGConfigs.REWARDS.rewardsMinesweeper.getStage2().getCategoryName()));
        rounds = processStageConfig(rounds, legacyGol.GameStageIII, cfgGol.getCategory(LGConfigs.GOL.stage3.getCategoryName()), cfgRewards.getCategory(LGConfigs.REWARDS.rewardsGol.getStage3().getCategoryName()), cfgRewards.getCategory(LGConfigs.REWARDS.rewardsMinesweeper.getStage3().getCategoryName()));
        processStageConfig(rounds, legacyGol.GameStageIV, cfgGol.getCategory(LGConfigs.GOL.stage4.getCategoryName()), cfgRewards.getCategory(LGConfigs.REWARDS.rewardsGol.getStage4().getCategoryName()), cfgRewards.getCategory(LGConfigs.REWARDS.rewardsMinesweeper.getStage4().getCategoryName()));

        LOGGER.info("Successfully migrated old config file!");

        reloadConfigs(LGConfigs.GENERAL, LGConfigs.GOL, LGConfigs.REWARDS);
        LOGGER.info("Configs reloaded!");
    }

    private static int processStageConfig(int startDigits, LegacyLGConfig.LootStageConfig legacyStage, ConfigCategory catStage, ConfigCategory catGolRewards, ConfigCategory catMsRewards) {
        int roundsStage2 = Math.max(legacyStage.MinDigitsRequired - startDigits, 1);
        catStage.get(ConfigGOL.Names.ROUNDS).set(roundsStage2);
        catStage.get(ConfigGOL.Names.RANDOMIZE_SEQUENCE).set(legacyStage.RandomizeSequence);
        catStage.get(ConfigGOL.Names.DISPLAY_TIME).set(legacyStage.DisplayTime * 20 / 1000);

        catGolRewards.get(RewardConfig.Names.MIN_ITEMS).set(legacyStage.MinItems);
        catGolRewards.get(RewardConfig.Names.MAX_ITEMS).set(legacyStage.MaxItems);
        catGolRewards.get(RewardConfig.Names.DEFAULT_LOOT_TABLE).set(legacyStage.LootTable);
        catGolRewards.get(RewardConfig.Names.PER_DIM_CONFIGS).set(legacyStage.DimensionalLoots.entrySet().stream().map(e -> e.getKey() + "|" + e.getValue().LootTable).toArray(String[]::new));

        catMsRewards.get(RewardConfig.Names.MIN_ITEMS).set(legacyStage.MinItems);
        catMsRewards.get(RewardConfig.Names.MAX_ITEMS).set(legacyStage.MaxItems);
        catMsRewards.get(RewardConfig.Names.DEFAULT_LOOT_TABLE).set(legacyStage.LootTable);
        catMsRewards.get(RewardConfig.Names.PER_DIM_CONFIGS).set(legacyStage.DimensionalLoots.entrySet().stream().map(e -> e.getKey() + "|" + e.getValue().LootTable).toArray(String[]::new));

        return legacyStage.MinDigitsRequired;
    }

    private static void reloadConfigs(Config... configs) {
        for (Config config : configs) {
            config.getConfig().save();
            config.load();
        }
    }
}
