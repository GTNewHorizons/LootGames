package ru.timeconqueror.lootgames.common.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.api.block.GameBlock;
import ru.timeconqueror.lootgames.common.block.tile.MSMasterTile;
import ru.timeconqueror.lootgames.common.config.ConfigMS;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.registry.LGAchievements;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.registry.LGSounds;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.WorldExt;

public class MSActivatorBlock extends GameBlock {
    public MSActivatorBlock() {
        setTextureName(LootGames.namespaced("ms_activator"));
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        if (!worldIn.isRemote) {
            BlockPos pos = BlockPos.of(x, y, z);

            ConfigMS.Snapshot snapshot = LGConfigs.MINESWEEPER.snapshot();
            int allocatedSize = snapshot.getStage4().getBoardSize();

            boolean succeed = LootGamesAPI.getFieldManager()
                    .trySetupBoard(((WorldServer) worldIn), pos, allocatedSize, 2, allocatedSize, LGBlocks.MS_MASTER, player)
                    .forTileIfSucceed(MSMasterTile.class, master -> master.init(snapshot))
                    .isSucceed();

            if (succeed) {
                WorldExt.playSoundServerly(worldIn, pos, LGSounds.MS_START_GAME, 0.6F, 1.0F);
                LGAchievements.MS_START.trigger(player);
            }
        }

        return true;
    }
}
