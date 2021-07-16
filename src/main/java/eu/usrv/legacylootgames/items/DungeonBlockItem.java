package eu.usrv.legacylootgames.items;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;


public class DungeonBlockItem extends ItemBlockWithMetadata {

    public DungeonBlockItem(Block pBlock) {
        super(pBlock, pBlock);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return this.getUnlocalizedName() + "_" + stack.getMetadata();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List tooltips, boolean p_77624_4_) {
        tooltips.add(EnumChatFormatting.DARK_GRAY + I18n.format("item.lootgames.dungeon_block.tooltip"));
    }
}
