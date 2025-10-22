package com.lootgames.sudoku.config;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;

import com.github.bsideup.jabel.Desugar;

import lombok.Getter;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.timecore.api.common.config.Config;
import ru.timeconqueror.timecore.api.common.config.ConfigSection;

public class ConfigSudoku extends Config {

    public int weight;
    public int timeout;

    public StageConfig level1;
    public StageConfig level2;
    public StageConfig level3;
    public StageConfig level4;

    public ConfigSudoku() {
        super("sudoku");
        level1 = new StageConfig(getKey(), "stage_1", "Regulates characteristics of stage 1.", 35);
        level2 = new StageConfig(getKey(), "stage_2", "Regulates characteristics of stage 2.", 45);
        level3 = new StageConfig(getKey(), "stage_3", "Regulates characteristics of stage 3.", 55);
        level4 = new StageConfig(getKey(), "stage_4", "Regulates characteristics of stage 4.", 64);
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

        timeout = config.getInt(
            "timeout",
            getKey(),
            30,
            10,
            Integer.MAX_VALUE,
            "How long does it take to timeout a game? Value is in seconds.\nIf player has been inactive for given time, the game will go to sleep. The next player can start the game from the beginning.");

        level1.init(config);
        level2.init(config);
        level3.init(config);
        level4.init(config);
        config.setCategoryComment(getKey(), "Regulates 'Sudoku' minigame blank cell counts.");
    }

    @Override
    public String getRelativePath() {
        return LGConfigs.resolve("games/" + getKey());
    }

    public ConfigSudokuSnapshot snapshot() {
        return new ConfigSudokuSnapshot(level1.snapshot(), level2.snapshot(), level3.snapshot(), level4.snapshot());
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

        public LevelSnapshot stage1;
        public LevelSnapshot stage2;
        public LevelSnapshot stage3;
        public LevelSnapshot stage4;

        public ConfigSudokuSnapshot(LevelSnapshot s1, LevelSnapshot s2, LevelSnapshot s3, LevelSnapshot s4) {
            this.stage1 = s1;
            this.stage2 = s2;
            this.stage3 = s3;
            this.stage4 = s4;
        }

        public static NBTTagCompound serialize(ConfigSudokuSnapshot snap) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("stage_1", LevelSnapshot.serialize(snap.stage1));
            tag.setTag("stage_2", LevelSnapshot.serialize(snap.stage2));
            tag.setTag("stage_3", LevelSnapshot.serialize(snap.stage3));
            tag.setTag("stage_4", LevelSnapshot.serialize(snap.stage4));
            return tag;
        }

        public static ConfigSudokuSnapshot deserialize(NBTTagCompound tag) {
            return new ConfigSudokuSnapshot(
                LevelSnapshot.deserialize(tag.getCompoundTag("stage_1")),
                LevelSnapshot.deserialize(tag.getCompoundTag("stage_2")),
                LevelSnapshot.deserialize(tag.getCompoundTag("stage_3")),
                LevelSnapshot.deserialize(tag.getCompoundTag("stage_4")));
        }

        public static ConfigSudokuSnapshot stub() {
            return new ConfigSudokuSnapshot(
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
