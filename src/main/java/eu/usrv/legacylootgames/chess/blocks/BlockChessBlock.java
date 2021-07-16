
package eu.usrv.legacylootgames.chess.blocks;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import eu.usrv.legacylootgames.chess.tiles.TEChessMasterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.LootGames;


public class BlockChessBlock extends Block implements ITileEntityProvider {
    @SideOnly(Side.CLIENT)
    protected IIcon mTexture;

    public BlockChessBlock() {
        super(Material.iron);
        setBlockUnbreakable();
        setUnlocalizedName("chessGame");
        setCreativeTab(LootGames.CREATIVE_TAB);
        setLightLevel(1.0F);
    }

    @Override
    public final boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        mTexture = register.registerIcon(String.format("LootGames:chess/chessmasterblock"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
        return mTexture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess iBlockAccess, int x, int y, int z, int side) {
        return mTexture;
    }

    @Override
    public boolean onBlockActivated(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer, int pSide, float pSubX, float pSubY, float pSubZ) {
        if (pWorld.isRemote)
            return true;
        else {
            if (!pWorld.isRemote && pWorld.getTileEntity(pX, pY, pZ) instanceof TEChessMasterBlock) {
                TEChessMasterBlock tileEntity = (TEChessMasterBlock) pWorld.getTileEntity(pX, pY, pZ);
                tileEntity.onBlockClickedByPlayer(null, pPlayer);
            }
            return true;
        }
    }

    @Override
    public TileEntity createNewTileEntity(World pWorld, int p_149915_2_) {
        return new TEChessMasterBlock();
    }
}
