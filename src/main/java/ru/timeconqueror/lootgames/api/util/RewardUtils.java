package ru.timeconqueror.lootgames.api.util;

import eu.usrv.legacylootgames.blocks.DungeonLightSource;
import eu.usrv.yamcore.auxiliary.ItemDescriptor;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ChestGenHooks;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.minigame.LootGame;
import ru.timeconqueror.lootgames.common.config.base.RewardConfig;
import ru.timeconqueror.lootgames.common.config.base.StagedRewardConfig.FourStagedRewardConfig;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.BlockState;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.timecore.api.util.HorizontalDirection;
import ru.timeconqueror.timecore.api.util.MathUtils;
import ru.timeconqueror.timecore.api.util.RandHelper;

public class RewardUtils {
    /**
     * Spawns lamp decoration and up to four chests depending on provided {@code rewardLevel}
     *
     * @param world        world where to spawn chest
     * @param centralPos   position around which chests will be spawned
     * @param rewardLevel  from 0 to 4 inclusive. Zero means no chests will be generated.
     * @param rewardConfig reward part of your game's config
     */
    public static void spawnFourStagedReward(WorldServer world, LootGame<?, ?> game, BlockPos centralPos, int rewardLevel, FourStagedRewardConfig rewardConfig) {
        BlockState state = BlockState.of(LGBlocks.DUNGEON_LAMP, DungeonLightSource.State.NORMAL.ordinal());
        WorldExt.setBlockState(world, centralPos.offset(1, 0, 1), state);
        WorldExt.setBlockState(world, centralPos.offset(1, 0, -1), state);
        WorldExt.setBlockState(world, centralPos.offset(-1, 0, 1), state);
        WorldExt.setBlockState(world, centralPos.offset(-1, 0, -1), state);

        rewardLevel = MathUtils.coerceInRange(rewardLevel, 0, 4);

        if (rewardLevel > 0) {
            int counter = 0;
            for (HorizontalDirection direction : HorizontalDirection.values()) {
                if (counter >= rewardLevel) break;

                spawnLootChest(world, centralPos, direction, SpawnChestData.fromRewardConfig(game, rewardConfig.getStageByIndex(rewardLevel - 1)));
                counter++;
            }
        }
    }

    /**
     * Spawns chest with provided {@link SpawnChestData} in 1 block direction from central position.
     *
     * @param world               world where to spawn chest
     * @param centralPos          position around which chest will be spawned
     * @param horizontalDirection in what direction of {@code centralPos} chest should be placed.
     *                            For example, if you set the direction to {@link HorizontalDirection#NORTH},
     *                            chest will be spawned 1 block north of the {@code centralPos} with south facing.
     * @param chestData           data which contains the rules of setting chest content
     */
    public static void spawnLootChest(WorldServer world, BlockPos centralPos, HorizontalDirection horizontalDirection, SpawnChestData chestData) {
        EnumFacing direction = horizontalDirection.get();
        String lootTable = chestData.getLootTableKey();

        WeightedRandomChestContent[] randomLoot = ChestGenHooks.getItems(lootTable, RandHelper.RAND);

        BlockPos pos = centralPos.offset(direction.getFrontOffsetX(), 0, direction.getFrontOffsetZ());
        WorldExt.setBlock(world, pos, Blocks.chest, 3);//FIXME what about block facing?
        IInventory chestTile = (IInventory) WorldExt.getTileEntity(world, pos);

        if (chestTile != null) {
            if (randomLoot.length == 0) {
                LootGames.LOGGER.error("Received LootTable is empty. Skipping Chest-Gen to avoid NPE Crash");
                ItemDescriptor tSorryItem = ItemDescriptor.fromString("minecraft:stone");
                ItemStack sorryStack = tSorryItem.getItemStackwNBT(1, String.format("{display:{Name:\"The Sorry-Stone\",Lore:[\"Modpack creator failed to configure the LootTables properly.\nPlease report that Loot Table [%s] for %s stage is broken, thank you!\"]}}", chestData.lootTableRL, chestData.getGameName()));
                chestTile.setInventorySlotContents(0, sorryStack);
            } else {
                int count = RandHelper.RAND.nextInt(chestData.getMaxItems()) + chestData.getMinItems();
                WeightedRandomChestContent.generateChestContents(RandHelper.RAND, randomLoot, chestTile, count);
            }
        }
    }

    public static class SpawnChestData {
        private final String lootTableRL;
        private final String gameName;
        private final int minItems;
        private final int maxItems;

        public static SpawnChestData fromRewardConfig(LootGame<?, ?> game, RewardConfig rewardConfig) {
            return new SpawnChestData(game, rewardConfig.getLootTable(game.getWorld()), rewardConfig.minItems, rewardConfig.maxItems);
        }

        /**
         * @param game        game, which calls this method
         * @param lootTableRL loot table, from which items will be set in spawned chest.
         *                    If loot table won't be found, the game will place "sorry-stone" in the chest.
         * @param minItems    minimum amount of item stacks to be generated in chest.
         *                    Won't be applied, if count of items in bound loot table are less than it.
         *                    If min and max are set to -1, the limits will be disabled.
         * @param maxItems    maximum amount of item stacks to be generated in chest.
         *                    If this is set to -1, max limit will be disabled.
         */
        public SpawnChestData(LootGame<?, ?> game, String lootTableRL, int minItems, int maxItems) {
            this.lootTableRL = lootTableRL;
            this.minItems = minItems;
            this.maxItems = maxItems;
            this.gameName = game.getClass().getSimpleName();
        }

        public String getGameName() {
            return gameName;
        }

        public int getMaxItems() {
            return maxItems;
        }

        public int getMinItems() {
            return minItems;
        }

        public String getLootTableKey() {
            return lootTableRL;
        }
    }
}