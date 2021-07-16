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
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.client.IconLoader;
import ru.timeconqueror.timecore.api.util.RandHelper;

import java.util.List;


public class DungeonBrick extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon[] mIcons;

    public DungeonBrick() {
        super(Material.rock);
        setUnlocalizedName("dungeonBrick");
        setCreativeTab(LootGames.CREATIVE_TAB);
        setHardness(10.0F);
        setResistance(6.0F);
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
        list.add(new ItemStack(item, 1, 2));
        list.add(new ItemStack(item, 1, 3));
        list.add(new ItemStack(item, 1, 4));
        list.add(new ItemStack(item, 1, 5));
        list.add(new ItemStack(item, 1, 6));
    }

    @Override
    public int damageDropped(int meta) {
        int tRetMeta = meta;

        if (meta == Type.CEILING.ordinal() || meta == Type.FLOOR.ordinal() || meta == Type.WALL.ordinal())
            tRetMeta = meta + RandHelper.flipCoin(0, 3);

        return tRetMeta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        mIcons = new IIcon[6];
        mIcons[0] = register.registerIcon("LootGames:dungeon_wall");
        mIcons[1] = register.registerIcon("LootGames:dungeon_ceiling");
        mIcons[2] = register.registerIcon("LootGames:dungeon_floor");
        mIcons[3] = register.registerIcon("LootGames:cracked_dungeon_wall");
        mIcons[4] = register.registerIcon("LootGames:cracked_dungeon_ceiling");
        mIcons[5] = register.registerIcon("LootGames:cracked_dungeon_floor");
    }

    @Override
    public int getHarvestLevel(int metadata) {
        int tHarvestLevel = super.getHarvestLevel(metadata);

        if (metadata == Type.CEILING.ordinal() || metadata == Type.FLOOR.ordinal() || metadata == Type.WALL.ordinal())
            tHarvestLevel = 4;
        else if (metadata == Type.CEILING_CRACKED.ordinal() || metadata == Type.FLOOR_CRACKED.ordinal() || metadata == Type.WALL_CRACKED.ordinal())
            tHarvestLevel = 2;

        return tHarvestLevel;
    }

    @Override
    public String getHarvestTool(int metadata) {
        return "pickaxe";
    }

    @Override
    public float getBlockHardness(World pWorld, int pX, int pY, int pZ) {
        int tMeta = pWorld.getBlockMetadata(pX, pY, pZ);
        float tBlockHardness = super.getBlockHardness(pWorld, pX, pY, pZ);

        if (tMeta == Type.FLOOR_SHIELDED.ordinal())
            tBlockHardness = -1F;
        else if (tMeta == Type.CEILING_CRACKED.ordinal() || tMeta == Type.FLOOR_CRACKED.ordinal() || tMeta == Type.WALL_CRACKED.ordinal())
            tBlockHardness = 2.0F;

        return tBlockHardness;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int pSide, int pMeta) {
        if (pMeta == 6) return IconLoader.shieldedDungeonFloor;
        return mIcons[pMeta];
    }

    public enum Type {
        WALL,
        CEILING,
        FLOOR,
        WALL_CRACKED,
        CEILING_CRACKED,
        FLOOR_CRACKED,
        FLOOR_SHIELDED
    }

}
