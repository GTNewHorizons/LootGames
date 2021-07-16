package eu.usrv.legacylootgames.auxiliary;


import com.google.common.collect.Lists;

import java.util.List;

/**
 * Extended ForgeDirections enum to cover a full block-circle
 */
public enum ExtendedDirections {
    /**
     * -Y
     */
    DOWN(0, -1, 0),

    /**
     * +Y
     */
    UP(0, 1, 0),

    /**
     * -Z
     */
    NORTH(0, 0, -1),

    /**
     * +Z
     */
    SOUTH(0, 0, 1),

    /**
     * -X
     */
    WEST(-1, 0, 0),

    /**
     * +X
     */
    EAST(1, 0, 0),

    /**
     * Combined
     **/
    NORTHEAST(1, 0, -1), NORTHWEST(-1, 0, -1), SOUTHEAST(1, 0, 1), SOUTHWEST(-1, 0, 1),

    /**
     * Used only by getOrientation, for invalid inputs
     */
    UNKNOWN(0, 0, 0);

    public static final ExtendedDirections[] VALID_DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST};
    public static final List<ExtendedDirections> HORIZONTAL = Lists.newArrayList(NORTH, SOUTH, WEST, EAST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST);
    public static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4, 9, 8, 7, 6, 10};
    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;
    public final int flag;

    // Left hand rule rotation matrix for all possible axes of rotation
  /*public static final int[][] ROTATION_MATRIX = {
      { 0, 1, 4, 5, 3, 2, 6 },
      { 0, 1, 5, 4, 2, 3, 6 },
      { 5, 4, 2, 3, 0, 1, 6 },
      { 4, 5, 2, 3, 1, 0, 6 },
      { 2, 3, 1, 0, 4, 5, 6 },
      { 3, 2, 0, 1, 4, 5, 6 },
      { 0, 1, 2, 3, 4, 5, 6 },
  };*/

    ExtendedDirections(int x, int y, int z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        flag = 1 << ordinal();
    }

    public static boolean isHorizontal(ExtendedDirections direction) {
        return HORIZONTAL.contains(direction);
    }

    public static ExtendedDirections getOrientation(int id) {
        if (id >= 0 && id < VALID_DIRECTIONS.length) {
            return VALID_DIRECTIONS[id];
        }
        return UNKNOWN;
    }

    public ExtendedDirections getOpposite() {
        return getOrientation(OPPOSITES[ordinal()]);
    }

  /*public ExtendedDirections getRotation( ForgeDirection axis )
  {
    return getOrientation( ROTATION_MATRIX[axis.ordinal()][ordinal()] );
  }*/
}
