package ru.timeconqueror.lootgames.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * <!-- spotless:off -->
 * Code adapted from https://github.com/ghewgill/puzzles/blob/master/mines.c
 *
 * Original License
 *
 * This software is copyright (c) 2004-2014 Simon Tatham.
 *
 * Portions copyright Richard Boulton, James Harvey, Mike Pinna, Jonas
 * Kölker, Dariusz Olszewski, Michael Schierl, Lambros Lambrou, Bernd
 * Schmidt, Steffen Bauer, Lennard Sprong and Rogier Goossens.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <!-- spotless:on -->
 */

public class MineSets {

    private final TreeSet<Integer> sets;
    private final TreeSet<Integer> todo;

    /*
     * 123 456 789
     */
    public MineSets() {
        this.sets = new TreeSet<>();
        this.todo = new TreeSet<>();
    }

    public void addSet(int set) {
        int mask = LogicMineSet.getSetMask(set);
        if (mask == 0) {
            System.out.println("Cannot add set with invalid mask. " + LogicMineSet.toString(set));
            return;
        }
        int x = LogicMineSet.getSetX(set);
        int y = LogicMineSet.getSetY(set);
        // System.out.println("Adding " + LogicMineSet.toString(set));

        // while ((mask & 0b111) == 0) {
        // mask >>= 3;
        // y++;
        // }
        int trailingZeros = Integer.numberOfTrailingZeros(mask);
        y = y + (trailingZeros / 3);
        mask >>= 3 * (trailingZeros / 3);

        while ((mask & 0b001001001) == 0) {
            mask >>= 1;
            x++;
        }
        set = LogicMineSet.Set(x, y, mask, LogicMineSet.getSetBombs(set));
        // System.out.println("Adding " +LogicMineSet.toString(set));
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Set mask contains illegal index");
        }
        if (this.sets.add(set)) this.todo.add(set);
    }

    public void removeSet(int set) {
        this.sets.remove(set);
        this.todo.remove(set);
    }

    public int getTodo() {
        // Get and remove a set from the todoSet
        int res = this.todo.first();
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
        int diffX = x - LogicMineSet.getSetX(setA);
        int diffY = y - LogicMineSet.getSetY(setA);
        if (diffX < 0 || diffX > 2 || diffY < 0 || diffY > 2) return false;
        int bit = diffX + 3 * diffY;
        return (LogicMineSet.getSetMask(setA) & (1 << bit)) != 0;
    }

    public int setMunge(int setA, int setB, boolean isDiff) {
        // Return the mask of the intersection or difference between two mine sets
        int diffX = LogicMineSet.getSetX(setB) - LogicMineSet.getSetX(setA);
        int diffY = LogicMineSet.getSetY(setB) - LogicMineSet.getSetY(setA);
        int maskA = LogicMineSet.getSetMask(setA);
        int maskB = LogicMineSet.getSetMask(setB);
        if (diffX >= 3 || diffX <= -3 || diffY >= 3 || diffY <= -3) {
            if (isDiff) return maskA;
            return 0;
        }
        while (diffX > 0) {
            maskB = maskB & ~(0b100100100);
            maskB <<= 1;
            diffX--;
        }
        while (diffX < 0) {
            maskB = maskB & ~(0b001001001);
            maskB >>= 1;
            diffX++;
        }
        while (diffY > 0) {
            maskB = maskB & ~(0b111000000);
            maskB <<= 3;
            diffY--;
        }
        while (diffY < 0) {
            maskB >>= 3;
            diffY++;
        }

        if (isDiff) {
            maskB = maskB ^ 0b111111111;
        }

        return maskA & maskB;
    }

    public List<Integer> setOverlap(int set) {
        // Find all the sets that overlap the given one in this's sets
        List<SortedSet<Integer>> sets = new ArrayList<>(5);
        int setX = LogicMineSet.getSetX(set);
        int setY = LogicMineSet.getSetY(set);
        int sizeGuess = 0;
        for (int dx = Math.max(-2, -setX); dx <= Math.min(2, LogicMineSet.posBitMask - setX); dx++) {
            byte lowY = (byte) (Math.max(-2, -setY) + setY);
            byte highY = (byte) (Math.min(2, LogicMineSet.posBitMask - setY) + setY);
            int lowKey = LogicMineSet.setSetX((byte) (setX + dx)) | LogicMineSet.setSetY(lowY);
            int highKey = LogicMineSet.setSetX((byte) (setX + dx)) | LogicMineSet.setSetY(highY) | 0xffff;
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
        for (int dx = Math.max(-2, -x); dx <= 0; dx++) {
            byte lowY = (byte) (Math.max(y - 2, 0));
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

    public int todoSize() {
        return this.todo.size();
    }

    public int getRandomSet() {
        // Todo change this to actually be random and not the highest left most set
        return this.sets.first();
    }

    public void clear() {
        this.sets.clear();
        this.todo.clear();
    }
}
