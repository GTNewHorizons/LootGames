package ru.timeconqueror.timecore.api.util;

import net.minecraft.util.EnumFacing;

public enum HorizontalDirection {
    NORTH(EnumFacing.NORTH),
    EAST(EnumFacing.EAST),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST);

    private final EnumFacing facing;

    HorizontalDirection(EnumFacing facing) {
        this.facing = facing;
    }

    public EnumFacing get() {
        return facing;
    }
}