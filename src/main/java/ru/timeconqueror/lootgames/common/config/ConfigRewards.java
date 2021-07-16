package ru.timeconqueror.lootgames.common.config;

import ru.timeconqueror.lootgames.common.config.base.StagedRewardConfig;
import ru.timeconqueror.timecore.api.common.config.Config;

public class ConfigRewards extends Config {
    public static final ConfigRewards INSTANCE = new ConfigRewards();

    public final StagedRewardConfig.FourStagedRewardConfig rewardsGol;
    public final StagedRewardConfig.FourStagedRewardConfig rewardsMinesweeper;

    public ConfigRewards() {
        super(Names.CATEGORY_REWARDS);

        rewardsGol = StagedRewardConfig.fourStaged(getKey(), LGConfigs.GOL.getKey(), "Game of Light Rewards", StagedRewards.fourStagedDefaults());
        rewardsMinesweeper = StagedRewardConfig.fourStaged(getKey(), LGConfigs.MINESWEEPER.getKey(), "Minesweeper Rewards", StagedRewards.fourStagedDefaults());
    }

    public static class Names {
        public static final String CATEGORY_REWARDS = "rewards";
    }

    @Override
    public void init() {
        rewardsGol.init(config);
        rewardsMinesweeper.init(config);
    }

    @Override
    public String getRelativePath() {
        return LGConfigs.resolve(getKey());
    }
}