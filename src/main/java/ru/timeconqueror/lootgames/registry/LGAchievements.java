package ru.timeconqueror.lootgames.registry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.AchievementPage;
import ru.timeconqueror.lootgames.LootGames;

import javax.annotation.Nullable;
import java.util.Arrays;

public enum LGAchievements {
    FIND_DUNGEON(resolve("find_dungeon"), new ItemStack(LGBlocks.PUZZLE_MASTER), 0, 0, null),
    WIN_GAME(resolve("win_game"), new ItemStack(Items.nether_star), 0, -2, LGAchievements.FIND_DUNGEON),
    LOSE_GAME(resolve("lose_game"), new ItemStack(Items.skull), 0, 2, LGAchievements.FIND_DUNGEON),
    MS_START(resolve("ms.start"), new ItemStack(LGBlocks.MS_ACTIVATOR), -2, 0, LGAchievements.FIND_DUNGEON),
    MS_BEAT_LEVEL4(resolve("ms.beat_level_4"), new ItemStack(Blocks.tnt), -4, 0, true, LGAchievements.MS_START),
    GOL_START(resolve("gol.start"), new ItemStack(LGBlocks.GOL_ACTIVATOR), 2, 0, LGAchievements.FIND_DUNGEON),
    GOL_MASTER_LEVEL3(resolve("gol.beat_level_3"), new ItemStack(Items.diamond), 4, -1, true, LGAchievements.GOL_START),
    GOL_MASTER_LEVEL4(resolve("gol.beat_level_4"), new ItemStack(Items.emerald), 4, 1, true, LGAchievements.GOL_START);

    private final Achievement achievement;

    LGAchievements(String name, ItemStack displayStack, int x, int y, @Nullable LGAchievements parent) {
        this(name, displayStack, x, y, false, parent);
    }

    LGAchievements(String name, ItemStack displayStack, int x, int y, boolean isSpecial, @Nullable LGAchievements parent) {
        this.achievement = new Achievement(name, name, x, y, displayStack, parent != null ? parent.get() : null).registerStat();
        if (parent == null)
            achievement.initIndependentStat();

        if (isSpecial)
            achievement.setSpecial();
    }

    public static void init() {
        AchievementPage.registerAchievementPage(new AchievementPage(StatCollector.translateToLocal("lootgames.achievement_page.name"), Arrays.stream(LGAchievements.values()).map(LGAchievements::get).toArray(Achievement[]::new)));
    }

    public Achievement get() {
        return achievement;
    }

    public void trigger(EntityPlayer player) {
        player.triggerAchievement(achievement);
    }

    public static String resolve(String name) {
        return LootGames.MODID + "." + name;
    }
}
