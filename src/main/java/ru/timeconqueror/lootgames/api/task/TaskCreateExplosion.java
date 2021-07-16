package ru.timeconqueror.lootgames.api.task;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.utils.future.BlockPos;
import ru.timeconqueror.lootgames.utils.future.WorldExt;

public class TaskCreateExplosion implements ITask {
    protected double x;
    private double y;
    private double z;
    private float strength;
    private boolean affectBlocks;

    public TaskCreateExplosion() {
    }

    public TaskCreateExplosion(BlockPos pos, float strength, boolean affectBlocks) {
        this(pos.getX(), pos.getY(), pos.getZ(), strength, affectBlocks);
    }

    public TaskCreateExplosion(double x, double y, double z, float strength, boolean affectBlocks) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.strength = strength;
        this.affectBlocks = affectBlocks;
    }

    @Override
    public void run(World world) {
        WorldExt.explode(world, null, x, y, z, strength, false);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound c = new NBTTagCompound();

        c.setDouble("x", x);
        c.setDouble("y", y);
        c.setDouble("z", z);
        c.setFloat("strength", strength);
        c.setBoolean("affect_blocks", affectBlocks);

        return c;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        x = nbt.getDouble("x");
        y = nbt.getDouble("y");
        z = nbt.getDouble("z");
        strength = nbt.getFloat("strength");
        affectBlocks = nbt.getBoolean("affect_blocks");
    }
}
