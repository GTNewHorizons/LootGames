package ru.timeconqueror.lootgames.api.util;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;

public class MineSets {

    private final TreeSet<Integer> sets;
    private final TreeSet<Integer> todo;

    /*
        123
        456
        789
     */
    public MineSets() {
        this.sets = new TreeSet<>();
        this.todo = new TreeSet<>();
    }
    public void addSet(int set) {
        short mask = LogicMineSet.getSetMask(set);
        assert (mask != 0);
        byte x = LogicMineSet.getSetX(set);
        int y = LogicMineSet.getSetY(set);

//        while ((mask & 0b111) != 0) {
//            mask >>= 3;
//            y--;
//        }
        int trailingZeros = Integer.numberOfTrailingZeros(mask);
        y = y - (trailingZeros / 3);
        mask >>= 3 * (trailingZeros / 3);

        while ((mask & 0b001001001) != 0) {
            mask >>= 1;
            x--;
        }
        set = LogicMineSet.setSetX(x)
                | (LogicMineSet.setSetY((byte) y))
                | LogicMineSet.setSetMask(mask)
                | LogicMineSet.setSetMines(LogicMineSet.getSetMines((set)));
        this.sets.add(set);
        this.todo.add(set);
    }
    public void removeSet(int set) {
        this.sets.remove(set);
        this.todo.remove(set);
    }
    public int getTodo() {
        // Get and remove a set from the todo set
        int res =  this.todo.first();
        this.todo.remove(res);
        return res;
    }
    public boolean hasTodo() {
        return !this.todo.isEmpty();
    }
    public int[] getAllSets() {
        int[] res = new int[this.sets.size()];
        int i = 0;
        for (int set : this.sets) {
            res[i] = set;
            i++;
        }
        return res;
    }

    public boolean setIncludes(int setA, int x, int y) {
        int diffX = LogicMineSet.getSetX(setA) - x;
        int diffY = LogicMineSet.getSetY(setA) - y;
        if (diffX < 0 || diffX > 2 || diffY < 0 || diffY > 2) return false;
        int bit = diffX + 3 * diffY;
        return (LogicMineSet.getSetMask(setA) & (1 << bit)) != 0;
    }

    public int setMunge(int setA, int setB, boolean isDiff){
        // Return the mask of the intersection or difference between two mine sets
        int diffX = LogicMineSet.getSetX(setA) - LogicMineSet.getSetX(setB);
        int diffY = LogicMineSet.getSetY(setA) - LogicMineSet.getSetY(setB);
        int maskA = LogicMineSet.getSetMask(setA);
        int maskB = LogicMineSet.getSetMask(setB);
        if (diffX >= 3 || diffX <= -3 || diffY >= 3 || diffY <= -3) {
            if (isDiff) return maskA;
            return 0;
        }
        if (diffX > 0) {
            while (diffX > 0) {
                diffX--;
                maskB <<= 1;
            }
        } else  {
            while (diffX < 0) {
                diffX++;
                maskB >>= 1;
            }
        }
        if (diffY > 0) {
            while (diffY > 0) {
                diffY--;
                maskB <<=3;
            }
        } else  {
            while (diffY < 0) {
                diffY++;
                maskB >>= 3;
            }
        }
        if (isDiff) {
            maskB = ~maskB;
        }

        return maskA & maskB;
    }

    public List<Integer> setOverlap(int set) {
        // Find all the sets that overlap the given one in this's sets
        List<SortedSet<Integer>> sets = new ArrayList<>(5);
        byte setX = LogicMineSet.getSetX(set);
        byte setY = LogicMineSet.getSetY(set);
        int sizeGuess = 0;
        for (int dx = Math.max(-2, -setX); dx <= Math.min(2, LogicMineSet.posBitMask - setX); dx ++) {
            byte lowY = (byte) (Math.max(-2, -setY) + setY);
            byte highY = (byte) (Math.min(2, LogicMineSet.posBitMask-setY) + setY);
            int lowKey = LogicMineSet.setSetX((byte) (setX + dx)) | LogicMineSet.setSetY(lowY);
            int highKey = LogicMineSet.setSetX((byte) (setX + dx)) | LogicMineSet.setSetY(highY) | 0xffff;;
            SortedSet<Integer> rawSets = this.sets.subSet(lowKey, highKey);
            sizeGuess += rawSets.size();
            sets.add(rawSets);
        }
        ArrayList<Integer> res = new ArrayList<>(sizeGuess / 2);
        for (SortedSet<Integer> s : sets) {
            for (int mineSet : s) {
                if (this.setMunge(mineSet, set, false) != 0) {
                    res.add(mineSet);
                }
            }
        }
        return res;
    }
    public List<Integer> setOverlap(byte x, byte y) {
        ArrayList<SortedSet<Integer>> sets = new ArrayList<>(3);

        int sizeGuess = 0;
        for (int dx = Math.max(-2, -x); dx <= 0; dx ++) {
            byte lowY = (byte) (Math.max(-2, -y) + y);
            int lowKey = LogicMineSet.setSetX((byte) (x + dx)) | LogicMineSet.setSetY(lowY);
            int highKey = LogicMineSet.setSetX((byte) (x + dx)) | LogicMineSet.setSetY(y) | 0xffff;
            SortedSet<Integer> rawSets = this.sets.subSet(lowKey, highKey);
            sizeGuess += rawSets.size();
            sets.add(rawSets);
        }
        ArrayList<Integer> res = new ArrayList<>(sizeGuess / 2);
        for (SortedSet<Integer> s : sets) {
            for (int mineSet : s) {
                if (this.setIncludes(mineSet, x, y)) {
                    res.add(mineSet);
                }
            }
        }
        return res;
    }

    public int size() {
        return this.sets.size();
    }

    public int getRandomSet() {
        return this.sets.first();
    }
}
