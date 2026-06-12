package ru.timeconqueror.lootgames.common.config;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;

import com.github.bsideup.jabel.Desugar;

import lombok.Getter;
import ru.timeconqueror.timecore.api.common.config.Config;
import ru.timeconqueror.timecore.api.common.config.ConfigSection;

public class ConfigSudoku extends Config {

    public enum ClearOnWrongAnswer {
        ALL,
        NONE,
        WRONG_ONLY
    }

    public int weight;
    public int attemptCount;
    public ClearOnWrongAnswer clearOnWrongAnswer;
    public boolean hintDuplicates;
    public boolean hintOverflow;
    public boolean hintCompletedDigit;
    public boolean hintCompletedSection;

    public StageConfig level1;
    public StageConfig level2;
    public StageConfig level3;
    public StageConfig level4;

    public ConfigSudoku() {
        super("sudoku");
        level1 = new StageConfig(getKey(), "stage_1", "Regulates characteristics of stage 1.", 25);
        level2 = new StageConfig(getKey(), "stage_2", "Regulates characteristics of stage 2.", 31);
        level3 = new StageConfig(getKey(), "stage_3", "Regulates characteristics of stage 3.", 37);
        level4 = new StageConfig(getKey(), "stage_4", "Regulates characteristics of stage 4.", 43);
    }

    @Override
    public void init() {
        weight = config.getInt(
                "weight",
                getKey(),
                1,
                0,
                Integer.MAX_VALUE,
                "How likely this game is chosen compared to other games. The higher this value is, the more likely this game is chosen. Set to 0 to turn this off.");

        attemptCount = config.getInt(
                "attempt_count",
                getKey(),
                3,
                1,
                Integer.MAX_VALUE,
                "Number of attempts the player has to submit a correct solution before the game ends.");

        String clearModeName = config.getString(
                "clear_on_wrong_answer",
                getKey(),
                ClearOnWrongAnswer.ALL.name(),
                "Controls which player-entered cells are cleared after a wrong submission.\n"
                        + "ALL - clear every player entry (forces a full redo).\n"
                        + "NONE - keep every entry, letting the player edit individual cells and resubmit.\n"
                        + "WRONG_ONLY - clear only cells whose value does not match the solution; correct entries stay.",
                new String[] { ClearOnWrongAnswer.ALL.name(), ClearOnWrongAnswer.NONE.name(),
                        ClearOnWrongAnswer.WRONG_ONLY.name() });
        try {
            clearOnWrongAnswer = ClearOnWrongAnswer.valueOf(clearModeName);
        } catch (IllegalArgumentException e) {
            clearOnWrongAnswer = ClearOnWrongAnswer.ALL;
        }

        String hintsCategory = getKey() + ".hints";
        hintDuplicates = config.getBoolean(
                "hint_duplicates",
                hintsCategory,
                false,
                "If true, cells containing duplicate digits in the same row, column or 3x3 box are highlighted yellow.");
        hintOverflow = config.getBoolean(
                "hint_overflow",
                hintsCategory,
                false,
                "If true, cells containing a digit that has been placed more than 9 times across the whole board are highlighted light red.");
        hintCompletedDigit = config.getBoolean(
                "hint_completed_digit",
                hintsCategory,
                false,
                "If true, every cell holding a digit is highlighted green once that digit has been placed exactly 9 times across the whole board.");
        hintCompletedSection = config.getBoolean(
                "hint_completed_section",
                hintsCategory,
                false,
                "If true, every cell in a row, column or 3x3 box that contains the digits 1-9 exactly once is highlighted cyan.");
        config.setCategoryComment(hintsCategory, "Visual hints rendered on the Sudoku board. All default to disabled.");

        level1.init(config);
        level2.init(config);
        level3.init(config);
        level4.init(config);
        config.setCategoryComment(getKey(), "Regulates 'Sudoku' minigame.");
    }

    @Override
    public String getRelativePath() {
        return LGConfigs.resolve("games/" + getKey());
    }

    public ConfigSudokuSnapshot snapshot() {
        return new ConfigSudokuSnapshot(
                attemptCount,
                clearOnWrongAnswer,
                hintDuplicates,
                hintOverflow,
                hintCompletedDigit,
                hintCompletedSection,
                level1.snapshot(),
                level2.snapshot(),
                level3.snapshot(),
                level4.snapshot());
    }

    public static class StageConfig extends ConfigSection {

        public int blanksCount;
        public int defaultBlanks;

        public StageConfig(String parent, String name, String comment, int defaultBlanks) {
            super(parent, name, comment);
            this.defaultBlanks = defaultBlanks;
        }

        public void init(Configuration config) {
            blanksCount = config.getInt("blanks", getCategoryName(), defaultBlanks, 0, 81, getComment());
            config.setCategoryComment(getCategoryName(), getComment());
        }

        public LevelSnapshot snapshot() {
            return new LevelSnapshot(blanksCount);
        }
    }

    @Getter
    public static class ConfigSudokuSnapshot {

        public int attemptCount;
        public ClearOnWrongAnswer clearOnWrongAnswer;
        public boolean hintDuplicates;
        public boolean hintOverflow;
        public boolean hintCompletedDigit;
        public boolean hintCompletedSection;
        public LevelSnapshot stage1;
        public LevelSnapshot stage2;
        public LevelSnapshot stage3;
        public LevelSnapshot stage4;

        public ConfigSudokuSnapshot(int attemptCount, ClearOnWrongAnswer clearOnWrongAnswer, boolean hintDuplicates,
                boolean hintOverflow, boolean hintCompletedDigit, boolean hintCompletedSection, LevelSnapshot s1,
                LevelSnapshot s2, LevelSnapshot s3, LevelSnapshot s4) {
            this.attemptCount = attemptCount;
            this.clearOnWrongAnswer = clearOnWrongAnswer;
            this.hintDuplicates = hintDuplicates;
            this.hintOverflow = hintOverflow;
            this.hintCompletedDigit = hintCompletedDigit;
            this.hintCompletedSection = hintCompletedSection;
            this.stage1 = s1;
            this.stage2 = s2;
            this.stage3 = s3;
            this.stage4 = s4;
        }

        public static NBTTagCompound serialize(ConfigSudokuSnapshot snap) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("attempt_count", snap.attemptCount);
            tag.setString("clear_on_wrong", snap.clearOnWrongAnswer.name());
            tag.setBoolean("hint_duplicates", snap.hintDuplicates);
            tag.setBoolean("hint_overflow", snap.hintOverflow);
            tag.setBoolean("hint_completed_digit", snap.hintCompletedDigit);
            tag.setBoolean("hint_completed_section", snap.hintCompletedSection);
            tag.setTag("stage_1", LevelSnapshot.serialize(snap.stage1));
            tag.setTag("stage_2", LevelSnapshot.serialize(snap.stage2));
            tag.setTag("stage_3", LevelSnapshot.serialize(snap.stage3));
            tag.setTag("stage_4", LevelSnapshot.serialize(snap.stage4));
            return tag;
        }

        public static ConfigSudokuSnapshot deserialize(NBTTagCompound tag) {
            int attempts = tag.hasKey("attempt_count") ? tag.getInteger("attempt_count") : 3;
            ClearOnWrongAnswer clearMode = ClearOnWrongAnswer.ALL;
            if (tag.hasKey("clear_on_wrong")) {
                try {
                    clearMode = ClearOnWrongAnswer.valueOf(tag.getString("clear_on_wrong"));
                } catch (IllegalArgumentException ignored) {}
            }
            return new ConfigSudokuSnapshot(
                    attempts,
                    clearMode,
                    tag.getBoolean("hint_duplicates"),
                    tag.getBoolean("hint_overflow"),
                    tag.getBoolean("hint_completed_digit"),
                    tag.getBoolean("hint_completed_section"),
                    LevelSnapshot.deserialize(tag.getCompoundTag("stage_1")),
                    LevelSnapshot.deserialize(tag.getCompoundTag("stage_2")),
                    LevelSnapshot.deserialize(tag.getCompoundTag("stage_3")),
                    LevelSnapshot.deserialize(tag.getCompoundTag("stage_4")));
        }

        public static ConfigSudokuSnapshot stub() {
            return new ConfigSudokuSnapshot(
                    3,
                    ClearOnWrongAnswer.ALL,
                    false,
                    false,
                    false,
                    false,
                    LevelSnapshot.stub(),
                    LevelSnapshot.stub(),
                    LevelSnapshot.stub(),
                    LevelSnapshot.stub());
        }

        public LevelSnapshot getStageByIndex(int idx) {
            return switch (idx) {
                case 1 -> stage1;
                case 2 -> stage2;
                case 3 -> stage3;
                case 4 -> stage4;
                default -> throw new IllegalArgumentException("Unknown stage index: " + idx);
            };
        }
    }

    @Desugar
    public record LevelSnapshot(int blanksCount) {

        public static NBTTagCompound serialize(LevelSnapshot s) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("blanks", s.blanksCount);
            return tag;
        }

        public static LevelSnapshot deserialize(NBTTagCompound tag) {
            return new LevelSnapshot(tag.getInteger("blanks"));
        }

        public static LevelSnapshot stub() {
            return new LevelSnapshot(0);
        }
    }
}
