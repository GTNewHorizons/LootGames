package ru.timeconqueror.lootgames.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import ru.timeconqueror.lootgames.client.IconLoader;

public abstract class GameBlock extends Block {
    public GameBlock() {
        super(Material.rock);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setLightLevel(1F);
    }

    public GameBlock(Material material) {
        super(material);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (this.textureName != null) return blockIcon;
        return IconLoader.shieldedDungeonFloor;
    }

    @Override
    public void registerIcons(IIconRegister reg) {
        if (this.textureName != null) {
            super.registerIcons(reg);
        }
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return false;
    }
}
