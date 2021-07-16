package ru.timeconqueror.lootgames.common.config.base;

import net.minecraftforge.common.config.Configuration;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.common.config.StagedRewards;
import ru.timeconqueror.timecore.api.common.config.ConfigSection;

public class StagedRewardConfig extends ConfigSection {
    private final RewardConfig[] configs;

    public static FourStagedRewardConfig fourStaged(String parentKey, String key, String comment, StagedRewards.FourStagedDefaults fourStagedDefaults) {
        return new FourStagedRewardConfig(parentKey, key, comment, fourStagedDefaults);
    }

    protected StagedRewardConfig(String parentKey, String key, String comment, RewardConfig... rewardConfigs) {
        super(parentKey, key, comment);
        this.configs = rewardConfigs;
    }

    public RewardConfig getStageByIndex(int index) {
        if (index < 0 || index > size() - 1) {
            throw new IllegalArgumentException("Provided number (=" + index + ") should be positive and less then " + (size() - 1));
        }
        return configs[index];
    }

    public int size() {
        return configs.length;
    }

    @Override
    public void init(Configuration config) {
        for (int i = 0; i < configs.length; i++) {
            RewardConfig rc = configs[i];
            rc.init(config);
            config.setCategoryComment(rc.getCategoryName(), "Rewards for stage " + (i + 1));
        }

        config.setCategoryComment(getCategoryName(), getComment());
    }

    public static class FourStagedRewardConfig extends StagedRewardConfig {
        private FourStagedRewardConfig(String parentKey, String key, String comment, StagedRewards.FourStagedDefaults fourStagedDefaults) {
            super(parentKey, key, comment,
                    new RewardConfig(LGConfigs.mergeCategories(parentKey, key), "stage_1", "Rewards for stage 1", fourStagedDefaults.getStage1()),
                    new RewardConfig(LGConfigs.mergeCategories(parentKey, key), "stage_2", "Rewards for stage 2", fourStagedDefaults.getStage2()),
                    new RewardConfig(LGConfigs.mergeCategories(parentKey, key), "stage_3", "Rewards for stage 3", fourStagedDefaults.getStage3()),
                    new RewardConfig(LGConfigs.mergeCategories(parentKey, key), "stage_4", "Rewards for stage 4", fourStagedDefaults.getStage4()));
        }

        public RewardConfig getStage1() {
            return getStageByIndex(0);
        }

        public RewardConfig getStage2() {
            return getStageByIndex(1);
        }

        public RewardConfig getStage3() {
            return getStageByIndex(2);
        }

        public RewardConfig getStage4() {
            return getStageByIndex(3);
        }
    }
}
