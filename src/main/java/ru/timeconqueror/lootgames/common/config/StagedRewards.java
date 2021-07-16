package ru.timeconqueror.lootgames.common.config;

import ru.timeconqueror.lootgames.common.config.base.RewardConfig.Defaults;
import ru.timeconqueror.lootgames.utils.sanity.LootTables;

public class StagedRewards {
    public static FourStagedDefaults fourStagedDefaults() {
        return FourStagedDefaults.DEFAULT;
    }

    public static FourStagedDefaults fourStagedDefaults(Defaults stage1, Defaults stage2, Defaults stage3, Defaults stage4) {
        return new FourStagedDefaults(stage1, stage2, stage3, stage4);
    }

    public static class FourStagedDefaults {
        private static final FourStagedDefaults DEFAULT = new FourStagedDefaults(new Defaults(LootTables.DUNGEON_CHEST, 4, 8),
                new Defaults(LootTables.MINESHAFT_CORRIDOR, 5, 10),
                new Defaults(LootTables.PYRAMID_JUNGLE_CHEST, 6, 11),
                new Defaults(LootTables.STRONGHOLD_CORRIDOR, 7, 12)
        );

        private final Defaults stage1;
        private final Defaults stage2;
        private final Defaults stage3;
        private final Defaults stage4;

        private FourStagedDefaults(Defaults stage1, Defaults stage2, Defaults stage3, Defaults stage4) {
            this.stage1 = stage1;
            this.stage2 = stage2;
            this.stage3 = stage3;
            this.stage4 = stage4;
        }

        public Defaults getStage1() {
            return stage1;
        }

        public Defaults getStage2() {
            return stage2;
        }

        public Defaults getStage3() {
            return stage3;
        }

        public Defaults getStage4() {
            return stage4;
        }
    }
}
