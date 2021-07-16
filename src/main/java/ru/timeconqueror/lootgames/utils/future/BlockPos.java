package ru.timeconqueror.lootgames.utils.future;

import com.google.common.collect.AbstractIterator;
import net.minecraft.util.EnumFacing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Immutable
public class BlockPos extends Vector3i {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * An immutable block pos with zero as all coordinates.
     */
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    private static final int PACKED_X_LENGTH = 1 + MathHelper.calculateLogBaseTwo(MathHelper.roundUpToPowerOfTwo(30000000));
    private static final int PACKED_Z_LENGTH = PACKED_X_LENGTH;
    private static final int PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
    private static final long PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
    private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
    private static final long PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
    private static final int Z_OFFSET = PACKED_Y_LENGTH;
    private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;

    public BlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    public BlockPos(double x, double y, double z) {
        super(x, y, z);
    }

    public BlockPos(Vector3i source) {
        this(source.getX(), source.getY(), source.getZ());
    }

    public static long offset(long pos, EnumFacing directionIn) {
        return offset(pos, directionIn.getFrontOffsetX(), directionIn.getFrontOffsetY(), directionIn.getFrontOffsetZ());
    }

    public static long offset(long pos, int dx, int dy, int dz) {
        return asLong(getX(pos) + dx, getY(pos) + dy, getZ(pos) + dz);
    }

    public static int getX(long packedPos) {
        return (int) (packedPos << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
    }

    public static int getY(long packedPos) {
        return (int) (packedPos << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
    }

    public static int getZ(long packedPos) {
        return (int) (packedPos << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
    }

    public static BlockPos of(long packedPos) {
        return new BlockPos(getX(packedPos), getY(packedPos), getZ(packedPos));
    }

    public static BlockPos of(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    public long asLong() {
        return asLong(this.getX(), this.getY(), this.getZ());
    }

    public static long asLong(int x, int y, int z) {
        long i = 0L;
        i = i | ((long) x & PACKED_X_MASK) << X_OFFSET;
        i = i | ((long) y & PACKED_Y_MASK) << 0;
        return i | ((long) z & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long packedPos) {
        return packedPos & -16L;
    }

    /**
     * Add the given coordinates to the coordinates of this BlockPos
     */
    public BlockPos offset(double x, double y, double z) {
        return x == 0.0D && y == 0.0D && z == 0.0D ? this : new BlockPos((double) this.getX() + x, (double) this.getY() + y, (double) this.getZ() + z);
    }

    /**
     * Add the given coordinates to the coordinates of this BlockPos
     */
    public BlockPos offset(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    /**
     * Add the given Vector to this BlockPos
     */
    public BlockPos offset(Vector3i vec) {
        return this.offset(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Subtract the given Vector from this BlockPos
     */
    public BlockPos subtract(Vector3i vec) {
        return this.offset(-vec.getX(), -vec.getY(), -vec.getZ());
    }

    /**
     * Offset this BlockPos 1 block up
     */
    public BlockPos above() {
        return this.relative(EnumFacing.UP);
    }

    /**
     * Offset this BlockPos n blocks up
     */
    public BlockPos above(int n) {
        return this.relative(EnumFacing.UP, n);
    }

    /**
     * Offset this BlockPos 1 block down
     */
    public BlockPos below() {
        return this.relative(EnumFacing.DOWN);
    }

    /**
     * Offset this BlockPos n blocks down
     */
    public BlockPos below(int n) {
        return this.relative(EnumFacing.DOWN, n);
    }

    /**
     * Offset this BlockPos 1 block in northern direction
     */
    public BlockPos north() {
        return this.relative(EnumFacing.NORTH);
    }

    /**
     * Offset this BlockPos n blocks in northern direction
     */
    public BlockPos north(int n) {
        return this.relative(EnumFacing.NORTH, n);
    }

    /**
     * Offset this BlockPos 1 block in southern direction
     */
    public BlockPos south() {
        return this.relative(EnumFacing.SOUTH);
    }

    /**
     * Offset this BlockPos n blocks in southern direction
     */
    public BlockPos south(int n) {
        return this.relative(EnumFacing.SOUTH, n);
    }

    /**
     * Offset this BlockPos 1 block in western direction
     */
    public BlockPos west() {
        return this.relative(EnumFacing.WEST);
    }

    /**
     * Offset this BlockPos n blocks in western direction
     */
    public BlockPos west(int n) {
        return this.relative(EnumFacing.WEST, n);
    }

    /**
     * Offset this BlockPos 1 block in eastern direction
     */
    public BlockPos east() {
        return this.relative(EnumFacing.EAST);
    }

    /**
     * Offset this BlockPos n blocks in eastern direction
     */
    public BlockPos east(int n) {
        return this.relative(EnumFacing.EAST, n);
    }

    /**
     * Offset this BlockPos 1 block in the given direction
     */
    public BlockPos relative(EnumFacing facing) {
        return new BlockPos(this.getX() + facing.getFrontOffsetX(), this.getY() + facing.getFrontOffsetY(), this.getZ() + facing.getFrontOffsetZ());
    }

    /**
     * Offsets this BlockPos n blocks in the given direction
     */
    public BlockPos relative(EnumFacing facing, int n) {
        return n == 0 ? this : new BlockPos(this.getX() + facing.getFrontOffsetX() * n, this.getY() + facing.getFrontOffsetY() * n, this.getZ() + facing.getFrontOffsetZ() * n);
    }

    /**
     * Calculate the cross product of this and the given Vector
     */
    public BlockPos cross(Vector3i vec) {
        return new BlockPos(this.getY() * vec.getZ() - this.getZ() * vec.getY(), this.getZ() * vec.getX() - this.getX() * vec.getZ(), this.getX() * vec.getY() - this.getY() * vec.getX());
    }

    /**
     * Returns a version of this BlockPos that is guaranteed to be immutable.
     *
     * <p>When storing a BlockPos given to you for an extended period of time, make sure you
     * use this in case the value is changed internally.</p>
     */
    public BlockPos immutable() {
        return this;
    }

    public BlockPos.Mutable mutable() {
        return new BlockPos.Mutable(this.getX(), this.getY(), this.getZ());
    }

    public static Iterable<BlockPos> randomBetweenClosed(Random rand, int amount, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int i = maxX - minX + 1;
        int j = maxY - minY + 1;
        int k = maxZ - minZ + 1;
        return () -> {
            return new AbstractIterator<BlockPos>() {
                final BlockPos.Mutable nextPos = new BlockPos.Mutable();
                int counter = amount;

                protected BlockPos computeNext() {
                    if (this.counter <= 0) {
                        return this.endOfData();
                    } else {
                        BlockPos blockpos = this.nextPos.set(minX + rand.nextInt(i), minY + rand.nextInt(j), minZ + rand.nextInt(k));
                        --this.counter;
                        return blockpos;
                    }
                }
            };
        };
    }

    /**
     * Returns BlockPos#getProximitySortedBoxPositions as an Iterator.
     */
    public static Iterable<BlockPos> withinManhattan(BlockPos pos, int xWidth, int yHeight, int zWidth) {
        int i = xWidth + yHeight + zWidth;
        int j = pos.getX();
        int k = pos.getY();
        int l = pos.getZ();
        return () -> {
            return new AbstractIterator<BlockPos>() {
                private final BlockPos.Mutable cursor = new BlockPos.Mutable();
                private int currentDepth;
                private int maxX;
                private int maxY;
                private int x;
                private int y;
                private boolean zMirror;

                protected BlockPos computeNext() {
                    if (this.zMirror) {
                        this.zMirror = false;
                        this.cursor.setZ(l - (this.cursor.getZ() - l));
                        return this.cursor;
                    } else {
                        BlockPos blockpos;
                        for (blockpos = null; blockpos == null; ++this.y) {
                            if (this.y > this.maxY) {
                                ++this.x;
                                if (this.x > this.maxX) {
                                    ++this.currentDepth;
                                    if (this.currentDepth > i) {
                                        return this.endOfData();
                                    }

                                    this.maxX = Math.min(xWidth, this.currentDepth);
                                    this.x = -this.maxX;
                                }

                                this.maxY = Math.min(yHeight, this.currentDepth - Math.abs(this.x));
                                this.y = -this.maxY;
                            }

                            int i1 = this.x;
                            int j1 = this.y;
                            int k1 = this.currentDepth - Math.abs(i1) - Math.abs(j1);
                            if (k1 <= zWidth) {
                                this.zMirror = k1 != 0;
                                blockpos = this.cursor.set(j + i1, k + j1, l + k1);
                            }
                        }

                        return blockpos;
                    }
                }
            };
        };
    }

    public static Optional<BlockPos> findClosestMatch(BlockPos pos, int width, int height, Predicate<BlockPos> posFilter) {
        return withinManhattanStream(pos, width, height, width).filter(posFilter).findFirst();
    }

    /**
     * Returns a stream of positions in a box shape, ordered by closest to furthest. Returns by definition the given
     * position as first element in the stream.
     */
    public static Stream<BlockPos> withinManhattanStream(BlockPos pos, int xWidth, int yHeight, int zWidth) {
        return StreamSupport.stream(withinManhattan(pos, xWidth, yHeight, zWidth).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(BlockPos firstPos, BlockPos secondPos) {
        return betweenClosed(Math.min(firstPos.getX(), secondPos.getX()), Math.min(firstPos.getY(), secondPos.getY()), Math.min(firstPos.getZ(), secondPos.getZ()), Math.max(firstPos.getX(), secondPos.getX()), Math.max(firstPos.getY(), secondPos.getY()), Math.max(firstPos.getZ(), secondPos.getZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(BlockPos firstPos, BlockPos secondPos) {
        return StreamSupport.stream(betweenClosed(firstPos, secondPos).spliterator(), false);
    }

    public static Stream<BlockPos> betweenClosedStream(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return StreamSupport.stream(betweenClosed(minX, minY, minZ, maxX, maxY, maxZ).spliterator(), false);
    }

    /**
     * Creates an Iterable that returns all positions in the box specified by the given corners. <strong>Coordinates must
     * be in order</strong>; e.g. x1 <= x2.
     * <p>
     * This method uses MutableBlockPos instead of regular BlockPos, which grants better
     * performance. However, the resulting BlockPos instances can only be used inside the iteration loop (as otherwise
     * the value will change), unless {@link Mutable#immutable()} is called. This method is ideal for searching large areas
     * and only storing a few locations.
     */
    public static Iterable<BlockPos> betweenClosed(int x1, int y1, int z1, int x2, int y2, int z2) {
        int i = x2 - x1 + 1;
        int j = y2 - y1 + 1;
        int k = z2 - z1 + 1;
        int l = i * j * k;
        return () -> new AbstractIterator<BlockPos>() {
            private final Mutable cursor = new Mutable();
            private int index;

            protected BlockPos computeNext() {
                if (this.index == l) {
                    return this.endOfData();
                } else {
                    int i1 = this.index % i;
                    int j1 = this.index / i;
                    int k1 = j1 % j;
                    int l1 = j1 / j;
                    ++this.index;
                    return this.cursor.set(x1 + i1, y1 + k1, z1 + l1);
                }
            }
        };
    }

    public static class Mutable extends BlockPos {
        public Mutable() {
            this(0, 0, 0);
        }

        public Mutable(int x_, int y_, int z_) {
            super(x_, y_, z_);
        }

        public Mutable(double x, double y, double z) {
            this(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
        }

        /**
         * Add the given coordinates to the coordinates of this BlockPos
         */
        public BlockPos offset(double x, double y, double z) {
            return super.offset(x, y, z).immutable();
        }

        /**
         * Add the given coordinates to the coordinates of this BlockPos
         */
        public BlockPos offset(int x, int y, int z) {
            return super.offset(x, y, z).immutable();
        }

        /**
         * Offsets this BlockPos n blocks in the given direction
         */
        public BlockPos relative(EnumFacing facing, int n) {
            return super.relative(facing, n).immutable();
        }

        /**
         * Sets position
         */
        public BlockPos.Mutable set(int xIn, int yIn, int zIn) {
            this.setX(xIn);
            this.setY(yIn);
            this.setZ(zIn);
            return this;
        }

        public BlockPos.Mutable set(double xIn, double yIn, double zIn) {
            return this.set(MathHelper.floor_double(xIn), MathHelper.floor_double(yIn), MathHelper.floor_double(zIn));
        }

        public BlockPos.Mutable set(Vector3i vec) {
            return this.set(vec.getX(), vec.getY(), vec.getZ());
        }

        public BlockPos.Mutable set(long packedPos) {
            return this.set(getX(packedPos), getY(packedPos), getZ(packedPos));
        }

        public BlockPos.Mutable setWithOffset(Vector3i pos, EnumFacing directionIn) {
            return this.set(pos.getX() + directionIn.getFrontOffsetX(), pos.getY() + directionIn.getFrontOffsetY(), pos.getZ() + directionIn.getFrontOffsetZ());
        }

        public BlockPos.Mutable setWithOffset(Vector3i pos, int offsetX, int offsetY, int offsetZ) {
            return this.set(pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ);
        }

        public BlockPos.Mutable move(EnumFacing facing) {
            return this.move(facing, 1);
        }

        public BlockPos.Mutable move(EnumFacing facing, int n) {
            return this.set(this.getX() + facing.getFrontOffsetX() * n, this.getY() + facing.getFrontOffsetY() * n, this.getZ() + facing.getFrontOffsetZ() * n);
        }

        public BlockPos.Mutable move(int xIn, int yIn, int zIn) {
            return this.set(this.getX() + xIn, this.getY() + yIn, this.getZ() + zIn);
        }

        public BlockPos.Mutable move(Vector3i vector3i_) {
            return this.set(this.getX() + vector3i_.getX(), this.getY() + vector3i_.getY(), this.getZ() + vector3i_.getZ());
        }

        /**
         * Sets the X coordinate.
         */
        public void setX(int xIn) {
            super.setX(xIn);
        }

        public void setY(int yIn) {
            super.setY(yIn);
        }

        /**
         * Sets the Z coordinate.
         */
        public void setZ(int zIn) {
            super.setZ(zIn);
        }

        /**
         * Returns a version of this BlockPos that is guaranteed to be immutable.
         *
         * <p>When storing a BlockPos given to you for an extended period of time, make sure you
         * use this in case the value is changed internally.</p>
         */
        public BlockPos immutable() {
            return new BlockPos(this);
        }
    }
}
