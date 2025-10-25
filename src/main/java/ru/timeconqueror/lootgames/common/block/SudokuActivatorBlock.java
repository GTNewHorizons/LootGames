package ru.timeconqueror.lootgames.common.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.api.block.GameBlock;
import ru.timeconqueror.lootgames.common.block.tile.SudokuTile;
import ru.timeconqueror.lootgames.common.config.ConfigSudoku;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.registry.LGSounds;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.WorldExt;

public class SudokuActivatorBlock extends GameBlock {

    public SudokuActivatorBlock() {
        setBlockTextureName(LootGames.namespaced("sdk_activator"));
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float hitX,
            float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            BlockPos pos = new BlockPos(x, y, z);
            // snapshot for Sudoku
            ConfigSudoku.ConfigSudokuSnapshot snapshot = LGConfigs.SUDOKU.snapshot();
            // Setup board area: using allocatedSize as both width and height
            boolean succeed = LootGamesAPI.getFieldManager()
                    .trySetupBoard((WorldServer) worldIn, pos, 9, 1, 9, LGBlocks.SDK_MASTER, player)
                    .forTileIfSucceed(SudokuTile.class, master -> master.init(snapshot)).isSucceed();

            if (succeed) {
                WorldExt.playSoundServerly(worldIn, pos, LGSounds.MS_START_GAME, 0.6F, 1.0F);
            }
        }
        return true;
    }
}
