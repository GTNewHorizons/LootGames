package ru.timeconqueror.lootgames.utils.future;

public class MathHelper {
    /**
     * A table of sin values computed from 0 (inclusive) to 2*pi (exclusive), with steps of 2*PI / 65536.
     */
    private static final float[] SIN_TABLE = new float[65536];
    /**
     * Though it looks like an array, this is really more like a mapping.  Key (index of this array) is the upper 5 bits
     * of the result of multiplying a 32-bit unsigned integer by the B(2, 5) De Bruijn sequence 0x077CB531.  Value
     * (value stored in the array) is the unique index (from the right) of the leftmost one-bit in a 32-bit unsigned
     * integer that can cause the upper 5 bits to get that value.  Used for highly optimized "find the log-base-2 of
     * this number" calculations.
     */
    private static final int[] multiplyDeBruijnBitPosition;

    /**
     * Returns the greatest integer less than or equal to the double argument
     */
    public static int floor_double(double p_76128_0_) {
        int i = (int) p_76128_0_;
        return p_76128_0_ < (double) i ? i - 1 : i;
    }

    /**
     * Returns the input value rounded up to the next highest power of two.
     */
    public static int roundUpToPowerOfTwo(int p_151236_0_) {
        int j = p_151236_0_ - 1;
        j |= j >> 1;
        j |= j >> 2;
        j |= j >> 4;
        j |= j >> 8;
        j |= j >> 16;
        return j + 1;
    }

    /**
     * Is the given value a power of two?  (1, 2, 4, 8, 16, ...)
     */
    private static boolean isPowerOfTwo(int p_151235_0_) {
        return p_151235_0_ != 0 && (p_151235_0_ & p_151235_0_ - 1) == 0;
    }

    /**
     * Uses a B(2, 5) De Bruijn sequence and a lookup table to efficiently calculate the log-base-two of the given
     * value.  Optimized for cases where the input value is a power-of-two.  If the input value is not a power-of-two,
     * then subtract 1 from the return value.
     */
    private static int calculateLogBaseTwoDeBruijn(int p_151241_0_) {
        p_151241_0_ = isPowerOfTwo(p_151241_0_) ? p_151241_0_ : roundUpToPowerOfTwo(p_151241_0_);
        return multiplyDeBruijnBitPosition[(int) ((long) p_151241_0_ * 125613361L >> 27) & 31];
    }

    /**
     * Efficiently calculates the floor of the base-2 log of an integer value.  This is effectively the index of the
     * highest bit that is set.  For example, if the number in binary is 0...100101, this will return 5.
     */
    public static int calculateLogBaseTwo(int p_151239_0_) {
        /**
         * Uses a B(2, 5) De Bruijn sequence and a lookup table to efficiently calculate the log-base-two of the given
         * value.  Optimized for cases where the input value is a power-of-two.  If the input value is not a power-of-
         * two, then subtract 1 from the return value.
         */
        return calculateLogBaseTwoDeBruijn(p_151239_0_) - (isPowerOfTwo(p_151239_0_) ? 0 : 1);
    }

    static {
        for (int var0 = 0; var0 < 65536; ++var0) {
            SIN_TABLE[var0] = (float) Math.sin((double) var0 * Math.PI * 2.0D / 65536.0D);
        }

        multiplyDeBruijnBitPosition = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
    }
}