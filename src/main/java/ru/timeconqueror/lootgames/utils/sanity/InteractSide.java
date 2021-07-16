package ru.timeconqueror.lootgames.utils.sanity;

import net.minecraft.util.EnumFacing;

public enum InteractSide {
    BOTTOM(EnumFacing.DOWN), TOP(EnumFacing.UP), EAST(EnumFacing.EAST), WEST(EnumFacing.WEST), NORTH(EnumFacing.NORTH), SOUTH(EnumFacing.SOUTH);

    private static final InteractSide[] VALUES = values();

    private final EnumFacing facing;

    InteractSide(EnumFacing facing) {
        this.facing = facing;
    }

    public static InteractSide byFace(int face) {
        return VALUES[face % VALUES.length];
    }

    public EnumFacing getFacing() {
        return facing;
    }
}
