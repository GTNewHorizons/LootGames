package ru.timeconqueror.lootgames.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.api.LootGamesAPI;
import ru.timeconqueror.lootgames.api.block.GameBlock;
import ru.timeconqueror.lootgames.common.block.tile.PuzzleMasterTile;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.lootgames.registry.LGAchievements;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.WorldExt;
import ru.timeconqueror.lootgames.utils.sanity.Particles;
import ru.timeconqueror.timecore.api.util.NetworkUtils;

import java.util.Optional;
import java.util.Random;

//TODO check surface before placing game block
//TODO add animation, which places block below
public class PuzzleMasterBlock extends GameBlock {
    @SideOnly(Side.CLIENT)
    protected IIcon icon;

    public PuzzleMasterBlock() {
        setLightLevel(1);
        setCreativeTab(LootGames.CREATIVE_TAB);
        setUnlocalizedName(LootGames.dotted("puzzle_master"));
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public void randomDisplayTick(World worldIn, int x, int y, int z, Random rand) {
        int particleCount = rand.nextInt(30);
        for (int i = 0; i <= particleCount; i++) {
            worldIn.spawnParticle(Particles.ENCHANT,
                    x + 0.5D + rand.nextGaussian() * 0.8D,
                    y + rand.nextFloat(),
                    z + 0.5D + rand.nextGaussian() * 0.8D,
                    rand.nextGaussian() * 0.02D,
                    0.5D + rand.nextGaussian() * 0.02D,
                    rand.nextGaussian() * 0.02D);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        if (!worldIn.isRemote) {
            try {
                if (LGConfigs.GENERAL.disableMinigames) {
                    NetworkUtils.sendMessage(player, new ChatComponentTranslation("msg.lootgames.puzzle_master.turned_off"));
                    return true;
                }

                BlockPos pos = BlockPos.of(x, y, z);
                Block block = WorldExt.getBlock(worldIn, pos);

                WorldExt.setBlock(worldIn, pos, Blocks.air);

                Optional<String> error = LootGamesAPI.getGameManager().generateRandomGame(worldIn, pos);
                if (!error.isPresent()) {
                    LGAchievements.FIND_DUNGEON.trigger(player);
                } else {
                    NetworkUtils.sendMessage(player, new ChatComponentText(error.get()));//TODO move error to lang file

                    WorldExt.setBlock(worldIn, pos, block, 2);//rollback
                }
            } catch (Throwable e) {
                NetworkUtils.sendMessage(player, new ChatComponentTranslation("msg.lootgames.puzzle_master.broken"));
                LootGames.LOGGER.error(e);
            }
        }

        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new PuzzleMasterTile();
    }

    @Override
    public void registerIcons(IIconRegister reg) {
        this.icon = reg.registerIcon(LootGames.namespaced("puzzle_master"));
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icon;
    }
}
