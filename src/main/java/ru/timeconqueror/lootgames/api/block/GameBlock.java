package ru.timeconqueror.lootgames.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.IBlockAccess;

public abstract class GameBlock extends Block {
    public GameBlock() {
        super(Material.rock);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setLightLevel(1 / 15F);
    }

    public GameBlock(Material material) {
        super(material);
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return false;
    }
}
