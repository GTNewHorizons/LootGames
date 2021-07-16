package ru.timeconqueror.lootgames.common.config;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.common.config.ConfigMS.Snapshot.StageSnapshot;
import ru.timeconqueror.timecore.api.common.config.Config;
import ru.timeconqueror.timecore.api.common.config.ConfigSection;

public class ConfigMS extends Config {
    private static final String NO_CHANGE_FOR_GENERATED = "Won't be changed for already generated Minesweeper boards!";

    public int detonationTime;
    public int attemptCount;

    public final StageConfig stage1;
    public final StageConfig stage2;
    public final StageConfig stage3;
    public final StageConfig stage4;

    public ConfigMS() {
        super("minesweeper");
        stage1 = new StageConfig(getKey(), "stage_1", "Regulates characteristics of stage 1.", new DefaultData(6, 20));
        stage2 = new StageConfig(getKey(), "stage_2", "Regulates characteristics of stage 2.", new DefaultData(7, 30));
        stage3 = new StageConfig(getKey(), "stage_3", "Regulates characteristics of stage 3.", new DefaultData(8, 42));
        stage4 = new StageConfig(getKey(), "stage_4", "Regulates characteristics of stage 4.", new DefaultData(9, 68));
    }

    @Override
    public void init() {
        detonationTime = config.getInt("detonation_time", getKey(), 3 * 20, 0, 600, "The time until bombs start to explode. Represented in ticks.");
        attemptCount = config.getInt("attempt_count", getKey(), 3, 1, Integer.MAX_VALUE, "It represents the number of attempts the player has to beat the game successfully.");

        stage1.init(config);
        stage2.init(config);
        stage3.init(config);
        stage4.init(config);

        config.setCategoryComment(getKey(), "Regulates \"Minesweeper\" minigame.");
    }

    /**
     * @param index - 1-4 (inclusive)
     *              Can return true, if index will be out of bounds.
     * @throws RuntimeException if stage config was not found for provided index.
     */
    public StageConfig getStageByIndex(int index) {
        switch (index) {
            case 1:
                return stage1;
            case 2:
                return stage2;
            case 3:
                return stage3;
            case 4:
                return stage4;
            default:
                throw new RuntimeException("Provided unknown stage config index " + index + ", please contact with mod author.");
        }
    }

    @Override
    public String getRelativePath() {
        return LGConfigs.resolve("games/" + getKey());
    }

    public Snapshot snapshot() {
        return new Snapshot(stage1.snapshot(), stage2.snapshot(), stage3.snapshot(), stage4.snapshot());
    }

    public static class StageConfig extends ConfigSection {
        private int bombCount;
        private int boardRadius;

        private final DefaultData defData;

        public StageConfig(String parentKey, String key, String comment, DefaultData defData) {
            super(parentKey, key, comment);
            this.defData = defData;
        }

        public StageSnapshot snapshot() {
            return new StageSnapshot(bombCount, getBoardSize());
        }

        public void init(Configuration config) {
            boardRadius = config.getInt("board_radius", getCategoryName(), defData.boardRadius, 2, 9, "The radius of Minesweeper board. " + NO_CHANGE_FOR_GENERATED);
            bombCount = config.getInt("bomb_count", getCategoryName(), defData.bombCount, 1, Integer.MAX_VALUE, "The amount of bombs on the board. Bomb count must be strictly less than amount of game fields (board_radius ^ 2). ", NO_CHANGE_FOR_GENERATED);

            int boardSize = getBoardSize();

            if (bombCount > boardSize * boardSize - 1) {
                LootGames.LOGGER.warn("Bomb count must be strictly less than amount of game fields. " +
                        "Current values: bomb count = {}, field count: {} (board size = {}, board radius = {})\n " +
                        "Bomb count was switched to {}.", bombCount, boardSize * boardSize, boardSize, boardSize * boardSize - 2, boardRadius);
                this.bombCount = boardSize * boardSize - 2; // at least 1 field with no bomb
            }

            config.setCategoryComment(getCategoryName(), getComment());
        }

        public int getBoardSize() {
            return boardRadius * 2 + 1;
        }
    }

    private static class DefaultData {
        private final int boardRadius;
        private final int bombCount;

        public DefaultData(int boardRadius, int bombCount) {
            this.boardRadius = boardRadius;
            this.bombCount = bombCount;
        }
    }

    public static class Snapshot {
        private final StageSnapshot stage1;
        private final StageSnapshot stage2;
        private final StageSnapshot stage3;
        private final StageSnapshot stage4;

        public Snapshot(StageSnapshot stage1, StageSnapshot stage2, StageSnapshot stage3, StageSnapshot stage4) {
            this.stage1 = stage1;
            this.stage2 = stage2;
            this.stage3 = stage3;
            this.stage4 = stage4;
        }

        public static NBTTagCompound serialize(Snapshot snapshot) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("stage_1", StageSnapshot.serialize(snapshot.stage1));
            compound.setTag("stage_2", StageSnapshot.serialize(snapshot.stage2));
            compound.setTag("stage_3", StageSnapshot.serialize(snapshot.stage3));
            compound.setTag("stage_4", StageSnapshot.serialize(snapshot.stage4));
            return compound;
        }

        public static Snapshot deserialize(NBTTagCompound serialized) {
            return new Snapshot(
                    StageSnapshot.deserialize(serialized.getCompoundTag("stage_1")),
                    StageSnapshot.deserialize(serialized.getCompoundTag("stage_2")),
                    StageSnapshot.deserialize(serialized.getCompoundTag("stage_3")),
                    StageSnapshot.deserialize(serialized.getCompoundTag("stage_4"))
            );
        }

        public StageSnapshot getStage1() {
            return stage1;
        }

        public StageSnapshot getStage2() {
            return stage2;
        }

        public StageSnapshot getStage3() {
            return stage3;
        }

        public StageSnapshot getStage4() {
            return stage4;
        }

        public static class StageSnapshot {
            private final int bombCount;
            private final int boardSize;

            public StageSnapshot(int bombCount, int boardSize) {
                this.bombCount = bombCount;
                this.boardSize = boardSize;
            }

            public int getBoardSize() {
                return boardSize;
            }

            public int getBombCount() {
                return bombCount;
            }

            private static NBTTagCompound serialize(StageSnapshot snapshot) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setInteger("bomb_count", snapshot.bombCount);
                compound.setInteger("board_size", snapshot.boardSize);
                return compound;
            }

            public static StageSnapshot deserialize(NBTTagCompound nbt) {
                return new StageSnapshot(nbt.getInteger("bomb_count"), nbt.getInteger("board_size"));
            }
        }

        /**
         * @param index - 1-4 (inclusive)
         *              Can return true, if index will be out of bounds.
         * @throws RuntimeException if stage config was not found for provided index.
         */
        public StageSnapshot getStageByIndex(int index) {
            switch (index) {
                case 1:
                    return stage1;
                case 2:
                    return stage2;
                case 3:
                    return stage3;
                case 4:
                    return stage4;
                default:
                    throw new RuntimeException("Provided unknown stage snapshot index " + index + ", please contact with mod author.");
            }
        }
    }
}
