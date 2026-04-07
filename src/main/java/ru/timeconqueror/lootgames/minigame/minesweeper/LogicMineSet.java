package ru.timeconqueror.lootgames.minigame.minesweeper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LogicMineSet {

    // A bit vector representation of a small set of mines around an x, y position
    // Upper byte is (signed) x pos
    // Next byte is (signed) y pos
    // Lowest nibble is the number of mines in the set
    // Bits 7-5 are unused
    // Bits 16-8 is a bit mask of which squares are in the set
    // Bit 8 == 1 means (x, y) is in the set
    // Bit 9 == 1 means (x+1, y) is in the set
    // Bit 16 == 1 means (x+2, y+2) is in the set
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
        return (y << 16) & 0x00ff0000;
    }

    public static int getSetMask(int set) {
        return ((set >> 7) & 0x1ff);
    }

    public static int setSetMask(int mask) {
        if (mask > 0x1ff || mask < 0) throw new IllegalArgumentException(
                "Cannot set mine set with illegal set mask: " + Integer.toBinaryString(mask));
        return mask << 7;
    }

    public static int setSetMask(int set, int mask) {
        if (mask > 0x1ff || mask < 0) throw new IllegalArgumentException(
                "Cannot set mine set with illegal set mask: " + Integer.toBinaryString(mask));
        set = (set & 0xffff007f) | LogicMineSet.setSetMask(mask);
        return set;
    }

    public static byte getSetBombs(int set) {
        return (byte) (set & 0xf);
    }

    public static int setSetMines(byte count) {
        if (count < 0 || count > 15)
            throw new IllegalArgumentException("Cannot create mine set with illegal bomb count: " + count);
        return count;
    }

    public static int setSetMines(int set, byte count) {
        if (count < 0 || count > 15)
            throw new IllegalArgumentException("Cannot set mine set with illegal bomb count: " + count);
        return (set & 0xfffffff0) | count;
    }

    public static int getMaskSquareCount(int mask) {
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

    public static String toString(int set) {
        int x = LogicMineSet.getSetX(set);
        int y = LogicMineSet.getSetY(set);
        int mask = LogicMineSet.getSetMask(set);
        int bombs = LogicMineSet.getSetBombs(set);
        return "Mineset {x: " + x + " y: " + y + " mask: " + Integer.toBinaryString(mask) + " bombs: " + bombs + "}";
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
