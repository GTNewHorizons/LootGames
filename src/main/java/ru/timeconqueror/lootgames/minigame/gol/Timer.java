package ru.timeconqueror.lootgames.minigame.gol;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;

public class Timer {
    private int ticks;

    public Timer(int ticks) {
        if (ticks < 0)
            throw new IllegalArgumentException("Provided number '" + ticks + "' should be greater or equal to 0");
        this.ticks = ticks;
    }

    public void update() {
        if (!ended()) {
            ticks--;
        }
    }

    public void reset(int ticks) {
        if (ticks < 0)
            throw new IllegalArgumentException("Provided number '" + ticks + "' should be greater or equal to 0");
        this.ticks = ticks;
    }

    public int ticksLeft() {
        return ticks;
    }

    public boolean ended() {
        return ticks == 0;
    }

    public static Timer fromNBT(NBTBase nbt) {
        return new Timer(((NBTTagInt) nbt).getInt());
    }

    public static NBTBase toNBT(Timer timer) {
        return new NBTTagInt(timer.ticksLeft());
    }
}
