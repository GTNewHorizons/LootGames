package eu.usrv.legacylootgames.blocks;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.timecore.api.util.RandHelper;

import java.util.List;
import java.util.Random;


public class DungeonLightSource extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon[] mIcons;

    public DungeonLightSource() {
        super(Material.glass);
        setUnlocalizedName("dungeonLight");
        setCreativeTab(LootGames.CREATIVE_TAB);
        setHardness(2.0F);
        setResistance(6.0F);
        setStepSound(soundTypeGlass);
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        mIcons = new IIcon[2];
        mIcons[0] = register.registerIcon("LootGames:dungeon_lamp");
        mIcons[1] = register.registerIcon("LootGames:broken_dungeon_lamp");
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return world.getBlockMetadata(x, y, z) == 0 ? 15 : 0;
    }

    @Override
    public int getHarvestLevel(int metadata) {
        if (metadata == State.BROKEN.ordinal())
            return 1;
        else
            return 4;
    }

    @Override
    public String getHarvestTool(int metadata) {
        return "pickaxe";
    }

    @Override
    public int quantityDropped(Random p_149745_1_) {
        return 1;
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    @Override
    public int damageDropped(int meta) {
        int tRet = State.BROKEN.ordinal();
        if (meta == State.NORMAL.ordinal())
            tRet = RandHelper.flipCoin(State.BROKEN.ordinal(), State.NORMAL.ordinal());

        return tRet;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int pSide, int pMeta) {
        return mIcons[pMeta];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess iBlockAccess, int x, int y, int z, int side) {
        return mIcons[iBlockAccess.getBlockMetadata(x, y, z)];
    }

    public enum State {
        NORMAL,
        BROKEN
    }

}
