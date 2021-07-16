package ru.timeconqueror.lootgames.common.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.api.block.GameBlock;
import ru.timeconqueror.lootgames.api.minigame.NotifyColor;
import ru.timeconqueror.lootgames.minigame.gol.GameOfLight;
import ru.timeconqueror.lootgames.registry.LGAchievements;
import ru.timeconqueror.lootgames.registry.LGBlocks;
import ru.timeconqueror.lootgames.registry.LGSounds;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.ChatComponentExt;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.timecore.api.util.NetworkUtils;

public class GOLActivatorBlock extends GameBlock {
    public GOLActivatorBlock() {
        setTextureName(LootGames.namespaced("gol_activator"));
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        if (!worldIn.isRemote) {
            BlockPos pos = BlockPos.of(x, y, z);
            boolean succeed = LootGamesAPI.getFieldManager()
                    .trySetupBoard(((WorldServer) worldIn), pos, GameOfLight.BOARD_SIZE, 2, GameOfLight.BOARD_SIZE, LGBlocks.GOL_MASTER, player).isSucceed();

            if (succeed) {
                NetworkUtils.sendMessage(player, ChatComponentExt.withStyle(new ChatComponentTranslation("msg.lootgames.gol.start"), NotifyColor.NOTIFY.getColor()));
                WorldExt.playSoundServerly(worldIn, pos, LGSounds.GOL_START_GAME, 0.75F, 1.0F);
                LGAchievements.GOL_START.trigger(player);
            }
        }

        return true;
    }
}
