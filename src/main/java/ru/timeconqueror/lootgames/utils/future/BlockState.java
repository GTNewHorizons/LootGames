package ru.timeconqueror.lootgames.utils.future;

import net.minecraft.block.Block;

import java.util.Objects;

public class BlockState {
    private final Block block;
    private final int meta;

    public BlockState(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }

    public static BlockState of(Block block, int meta) {
        return new BlockState(block, meta);
    }

    public Block getBlock() {
        return block;
    }

    public int getMeta() {
        return meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockState)) return false;
        BlockState that = (BlockState) o;
        return meta == that.meta && block.equals(that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, meta);
    }
}
