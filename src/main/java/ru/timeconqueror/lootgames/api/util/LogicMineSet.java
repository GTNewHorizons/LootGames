package ru.timeconqueror.lootgames.api.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LogicMineSet {

    // 0b xXXXXXXX yYYYYYYY MMMMMMMM MuuuCCCC
    public static int posBitMask = 255;

    public static byte getSetX(int set) {
        return (byte) (set >> 24);
    }

    public static int setSetX(byte x) {
        return x << 24;
    }

    public static byte getSetY(int set) {
        return (byte) (set >> 16);
    }

    public static int setSetY(byte y) {
        return y << 16;
    }

    public static short getSetMask(int set) {
        return (short) (set >> 7 & 0x1ff);
    }

    public static int setSetMask(int mask) {
        assert (mask <= 0x1ff && mask >= 0);
        return (int) mask << 7;
    }

    public static int setSetMask(int set, int mask) {
        assert (mask <= 0x1ff && mask >= 0);
        set = (set & 0xffff007f) | LogicMineSet.setSetMask(mask);
        return set;
    }

    public static byte getSetBombs(int set) {
        return (byte) (set & 0xf);
    }

    public static int setSetMines(byte count) {
        assert (count >= 0 & count <= 15);
        return count;
    }

    public static int setSetMines(int set, byte count) {
        assert (count >= 0 & count <= 15);
        return (set & 0xfffffff0) | count;
    }

    public static int getMaskSquareCount(int mask) {
        // int count = (mask & 0xAAAA >> 1) + (mask & 0x5555);
        // count = (count & 0xCCCC >> 2) + (count & 0x3333);
        // count = (count & 0xF0F0 >> 4) + (count & 0x0F0F);
        // count = (count & 0xFF00 >> 8) + (count & 0x00FF);
        // return count;
        return Integer.bitCount(mask);
    }

    public static int Set(int x, int y, int mask, int count) {
        return LogicMineSet.setSetX((byte) x) | LogicMineSet.setSetY((byte) y)
                | LogicMineSet.setSetMask(mask)
                | LogicMineSet.setSetMines((byte) count);
    }

    public static Iterable<Integer> iterateSetMask(int set) {
        return () -> new java.util.Iterator<>() {

            int remaining = LogicMineSet.getSetMask(set);

            @Override
            public boolean hasNext() {
                return remaining != 0;
            }

            @Override
            public Integer next() {
                int index = Integer.numberOfTrailingZeros(remaining);
                remaining &= remaining - 1;
                return index;
            }
        };
    }

    // n choose k
    public static Iterable<List<Integer>> permutations(int n, int k) {
        return () -> new java.util.Iterator<>() {

            int currentI = n;
            Iterator<List<Integer>> subIter = k > 0 ? permutations(n - 1, k - 1).iterator() : null;

            @Override
            public boolean hasNext() {
                return currentI >= k;
            }

            @Override
            public List<Integer> next() {
                if (k == 0) {
                    currentI = -1;
                    return new LinkedList<>();
                }
                List<Integer> res = subIter.next();
                res.add(currentI - 1);
                if (!subIter.hasNext()) {
                    currentI--;
                    subIter = permutations(currentI - 1, k - 1).iterator();
                }
                return res;
            }
        };
    }
}
