package ru.timeconqueror.lootgames.api.task;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.timeconqueror.lootgames.LootGames;
import ru.timeconqueror.lootgames.utils.future.INBTSerializable;
import ru.timeconqueror.timecore.api.exception.NotExistsException;

import java.util.ArrayList;
import java.util.Iterator;

public class TETaskScheduler implements INBTSerializable<NBTTagList> {
    private final ArrayList<TaskWrapper> tasks = new ArrayList<>();

    private final TileEntity tileEntity;

    public TETaskScheduler(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    /**
     * Thread-safely adds task to scheduler.
     */
    public void addTask(ITask task, int timeBeforeStart) {
        tasks.add(new TaskWrapper(timeBeforeStart, task));
    }

    /**
     * Must be called to update scheduler.
     */
    public void onUpdate() {
        Iterator<TaskWrapper> iterator = tasks.iterator();

        while (iterator.hasNext()) {
            TaskWrapper task = iterator.next();

            if (task.timeBeforeStart <= 0) {
                task.run(tileEntity.getWorld());
                iterator.remove();
            } else {
                task.decreaseTimer();
            }
        }
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList out = new NBTTagList();

        for (TaskWrapper wrapper : tasks) {
            NBTTagCompound element = new NBTTagCompound();

            element.setTag("task", wrapper.task.serializeNBT());
            element.setInteger("time", wrapper.timeBeforeStart);
            element.setString("name", wrapper.task.getClass().getName());

            out.appendTag(element);
        }

        return out;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deserializeNBT(NBTTagList nbt) {
        for (int i = 0; i < nbt.tagCount(); i++) {

            NBTTagCompound c = nbt.getCompoundTagAt(i);
            int time = c.getInteger("time");

            Class<?> clazz = null;
            try {
                clazz = Class.forName(c.getString("name"));
                Class<? extends ITask> taskClass = (Class<? extends ITask>) clazz;

                ITask task = TaskRegistry.createTask(taskClass);

                task.deserializeNBT(c.getCompoundTag("task"));

                tasks.add(new TaskWrapper(time, task));
            } catch (ClassNotFoundException e) {
                LootGames.LOGGER.error("Can't found class for name: {} . Skipping...", c.getString("name"));
            } catch (ClassCastException e) {
                LootGames.LOGGER.error("Restored class name {} doesn't inherit {}. Skipping...", clazz, ITask.class);
                e.printStackTrace();
            } catch (NotExistsException e) {
                LootGames.LOGGER.error("Mod author didn't register factory for task class {} in TaskRegistry. Skipping...", clazz);
                e.printStackTrace();
            }
        }
    }

    private static class TaskWrapper {
        private int timeBeforeStart;
        private final ITask task;

        private TaskWrapper(int timeBeforeStart, ITask task) {
            this.timeBeforeStart = timeBeforeStart;
            this.task = task;
        }

        private void decreaseTimer() {
            timeBeforeStart--;
        }

        private void run(World world) {
            task.run(world);
        }
    }
}
