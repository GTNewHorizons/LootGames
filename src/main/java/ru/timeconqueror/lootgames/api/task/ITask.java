package ru.timeconqueror.lootgames.api.task;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.utils.future.INBTSerializable;

/**
 * While implementing ITask instance, you should also register {@link ITaskFactory} in {@link TaskRegistry}/
 */
public interface ITask extends INBTSerializable<NBTTagCompound> {
    void run(World world);

    interface ITaskFactory<T extends ITask> {
        T create();
    }
}
