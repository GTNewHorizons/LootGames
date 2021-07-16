package ru.timeconqueror.lootgames.utils.future;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class WorldExt {
    public static BlockState getBlockState(World world, BlockPos pos) {
        return new BlockState(world.getBlock(pos.getX(), pos.getY(), pos.getZ()), world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ()));
    }

    public static Block getBlock(World world, BlockPos pos) {
        return world.getBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public static int getMeta(World world, BlockPos pos) {
        return world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());
    }

    public static TileEntity getTileEntity(World world, BlockPos pos) {
        return world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
    }

    public static void setBlock(World world, BlockPos pos, Block block, int meta, int flags) {
        world.setBlock(pos.getX(), pos.getY(), pos.getZ(), block, meta, flags);
    }

    public static void setBlock(World world, BlockPos pos, Block block, int flags) {
        setBlock(world, pos, block, 0, flags);
    }

    public static void setBlock(World world, BlockPos pos, Block block) {
        setBlock(world, pos, block, 3);
    }

    public static void setBlockState(World world, BlockPos pos, BlockState state) {
        setBlock(world, pos, state.getBlock(), state.getMeta(), 3);
    }

    public static void playSoundServerly(World world, BlockPos pos, String sound, float volume, float pitch) {
        world.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), sound, volume, pitch);
    }

    public static void playSoundCliently(World world, BlockPos pos, String sound, float volume, float pitch, boolean distanceDelay) {
        world.playSound(pos.getX(), pos.getY(), pos.getZ(), sound, volume, pitch, distanceDelay);
    }

    public static void explode(World world, @Nullable Entity exploder, BlockPos pos, float strength, boolean affectBlocks) {
        explode(world, exploder, pos.getX(), pos.getY(), pos.getZ(), strength, affectBlocks);
    }

    public static void explode(World world, @Nullable Entity exploder, double x, double y, double z, float strength, boolean affectBlocks) {
        world.createExplosion(exploder, x, y, z, strength, affectBlocks);
    }

    public static void setBlockToAir(World world, BlockPos pos) {
        world.setBlock(pos.getX(), pos.getY(), pos.getZ(), Blocks.air, 0, 3);
    }
}
