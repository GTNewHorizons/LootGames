package ru.timeconqueror.lootgames.minigame.minesweeper;

import java.util.*;

import ru.timeconqueror.lootgames.api.util.LogicMineSet;
import ru.timeconqueror.lootgames.api.util.MineSets;
import ru.timeconqueror.lootgames.api.util.Pos2i;

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


public class MSBoardSolver {

    private MSBoard board;
    private ArrayList<Pos2i> squaresTodo;
    private Type[] boardKnowledge;
    private ArrayList<Pos2i> revealStack;
    private MineSets setStore;

    public MSBoardSolver(MSBoard board) {
        if (board.size() > 125) {
            // The set store may not find range queries correctly on x over the 127-129 range,
            // as the bit vector changes sign.
            // Also it may take too long to solve boards over 8 chunks in size.
            throw new IllegalArgumentException("Cannot solve too large of a Minesweeper Board.");
        }
        this.board = board;
        this.squaresTodo = new ArrayList<Pos2i>(board.size() * board.size());
        this.boardKnowledge = new Type[board.size() * board.size()];
        for (int i = 0; i < board.size() * board.size(); i++) this.boardKnowledge[i] = Type.SOLVER_HIDDEN;
        this.revealStack = new ArrayList<>(board.size() * board.size());
        this.setStore = new MineSets();
    }

    private void addSquareToDo(Pos2i index) {
        this.squaresTodo.add(index);
    }

    private void revealSquares(int set, boolean isMine) {
        int bit = 0b1;
        int setMask = LogicMineSet.getSetMask(set);
        if (setMask == 0) return;
        byte setX = LogicMineSet.getSetX(set);
        byte setY = LogicMineSet.getSetY(set);
        for (int dy = 0; dy < 3; dy++) {
            for (int dx = 0; dx < 3; dx++) {
                if ((setMask & bit) != 0) {
                    this.revealSquare(new Pos2i(setX + dx, setY + dy), isMine);
                }
                bit <<= 1;
            }
        }
    }

    private void revealSquare(Pos2i pos, boolean isMine) {
        this.revealStack.add(pos);
        Type revealedSquare;
        if (isMine) {
            revealedSquare = Type.BOMB;
        } else {
            revealedSquare = this.board.getType(pos);
            // This will likely change when perturbations exist
        }
        this.boardKnowledge[this.board.toIndex(pos)] = revealedSquare;
        this.addSquareToDo(pos);
    }

    private Type getKnownField(Pos2i pos) {
        return this.boardKnowledge[this.board.toIndex(pos)];
    }

    private void processTodoSquares() {
        for (Pos2i pos : this.squaresTodo) {
            Type cellType = this.getKnownField(pos);
            boolean isRevealedMine = false;
            if (cellType == Type.BOMB) {
                isRevealedMine = true;
            } else {

                // Create a bomb set around this square
                byte bombs = cellType.getId();
                int mask = 0;
                int bit = 0b1;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (pos.getX() + dx < 0 || pos.getX() + dx >= this.board.size()
                                || pos.getY() + dy < 0
                                || pos.getY() + dy >= this.board.size())
                            continue;
                        Type adjField = this.getKnownField(pos);
                        if (adjField == Type.SOLVER_HIDDEN) {
                            mask = mask | bit;
                        } else if (adjField == Type.BOMB) {
                            bombs--;
                        }
                        bit <<= 1;

                    }
                }
                if (mask != 0) {
                    int set = LogicMineSet.Set(pos.getX() - 1, pos.getY() - 1, mask, bombs);
                    this.setStore.addSet(set);
                }
            }

            // Remove the revealed square from the mask of all known sets
            List<Integer> modifiedSets = this.setStore.setOverlap((byte) pos.getX(), (byte) pos.getY());
            for (int modSet : modifiedSets) {
                int newMask = this.setStore.setMunge(modSet, LogicMineSet.Set(pos.getX(), pos.getY(), 1, 0), true);

                this.setStore.removeSet(modSet);
                if (newMask != 0) {
                    int newSet = LogicMineSet.setSetMask(modSet, newMask);
                    if (isRevealedMine) newSet--;
                    this.setStore.addSet(newSet);
                }
            }
        }

        this.squaresTodo.clear();
    }

    private boolean processTodoSets() {
        boolean foundLogic = false;
        setLoop: while (this.setStore.hasTodo() && !foundLogic) {
            int todoSet = this.setStore.getTodo();
            // Check if the set has no bombs, or bombs == bitCount(mask)
            // which are trivially known squares
            byte todoBombs = LogicMineSet.getSetBombs(todoSet);

            int todoSquares = LogicMineSet.getMaskSquareCount(LogicMineSet.getSetMask(todoSet));
            if (todoBombs == 0 || todoBombs == todoSquares) {
                this.revealSquares(todoSet, todoBombs == todoSquares);
                this.setStore.removeSet(todoSet);
                foundLogic = true;
                break;
            }
            // Otherwise find all overlapping sets and attempt deductions
            List<Integer> overlappingSets = this.setStore.setOverlap(todoSet);
            for (int otherSet : overlappingSets) {
                // Find the non overlapping parts of otherSet and todoSet
                int wing1 = this.setStore.setMunge(todoSet, otherSet, true);
                int wing2 = this.setStore.setMunge(otherSet, todoSet, true);
                int middleMask = this.setStore.setMunge(todoSet, otherSet, false);
                int wing1Squares = LogicMineSet.getMaskSquareCount(wing1);
                int wing2Squares = LogicMineSet.getMaskSquareCount(wing2);
                int otherBombs = LogicMineSet.getSetBombs(otherSet);

                // Check if the cardinality of one set's wing is the same as the difference between the sets bombs
                // then the wing of the set with more bombs must be full, and the other wing empty
                if (wing1Squares == todoBombs - otherBombs || wing2Squares == otherBombs - todoBombs) {
                    boolean isWing1Mines = wing1Squares == todoBombs - otherBombs;
                    revealSquares(wing1, isWing1Mines);
                    revealSquares(wing2, !isWing1Mines);
                    int foundMines = isWing1Mines ? wing1Squares : wing2Squares;
                    // Processing todoSquares will clear the sets, but clear the current sets early
                    this.setStore.removeSet(todoSet);
                    this.setStore.removeSet(otherSet);
                    this.setStore.addSet(LogicMineSet.setSetMask(todoSet, middleMask) - foundMines // since the count is
                                                                                                   // the LSD can
                                                                                                   // directly subtract
                    );
                    foundLogic = true;
                    break setLoop;
                }

                // Otherwise check if one set is a subset of the other.
                if (wing1Squares == 0) {
                    // setTodo is a subset of otherSet
                    this.setStore.removeSet(otherSet);
                    this.setStore.addSet(LogicMineSet.setSetMask(otherSet, wing2) - todoBombs);
                    foundLogic = true;
                    break setLoop;
                } else if (wing2Squares == 0) {
                    // otherSet is a subset of setTodo
                    this.setStore.removeSet(todoSet);
                    this.setStore.addSet(LogicMineSet.setSetMask(todoSet, wing1) - otherBombs);
                    foundLogic = true;
                    break setLoop;
                }
            }
        }
        return foundLogic;
    }

    /**
     * @param sets The LMS ints
     * @return A list, each adjacent pair is the [start, end) indices of sets which overlap, (the array is mutated)
     */
    private List<Integer> partitionSets(int[] sets) {
        List<Integer> partitionEnd = new ArrayList<>(8);
        int partitionEndI = 1;
        for (int parentI = 0; parentI < sets.length; parentI++) {
            int parentSet = sets[parentI];
            if (parentI == partitionEndI) {
                partitionEnd.add(partitionEndI);
                partitionEndI++;
            }

            for (int otherI = partitionEndI; otherI < sets.length; otherI++) {
                int otherSet = sets[otherI];
                if (this.setStore.setMunge(parentSet, otherSet, false) != 0) {
                    // These sets overlap so add it to the partition
                    int temp = sets[otherI];
                    sets[otherI] = sets[partitionEndI];
                    sets[partitionEndI] = temp;
                    partitionEndI++;
                }
            }
        }
        partitionEnd.add(partitionEndI);
        return partitionEnd;
    }

    private Set<Integer> findAvailableIndices(int[] sets, int from, int to) {
        Set<Integer> res = new TreeSet<>();
        for (int i = from; i < to; i++) {
            int set = sets[i];
            int x = LogicMineSet.getSetX(set);
            int y = LogicMineSet.getSetY(set);
            int mask = LogicMineSet.getSetMask(set);
            int bit = 1;
            for (int dy = 0; dy <= 2; dy++) {
                for (int dx = 0; dx <= 2; dx++) {
                    if ((mask & bit) != 0) {
                        res.add(x + dx + (this.board.size() * (y + dy)));
                    }
                    bit <<= 1;
                }
            }
        }
        return res;
    }

    public int solve(Pos2i startFieldPos) {
        if (!this.board.isGenerated()) {
            this.board.generate(startFieldPos);
        }
        this.revealSquare(startFieldPos, false);
        for (int loops = 0; loops < 1000; loops++) {
            boolean doneSomething = !this.squaresTodo.isEmpty();
            this.processTodoSquares();
            doneSomething = doneSomething || this.processTodoSets();
            if (doneSomething) continue;

            /*
             * We have nothing left on our todolist, which means all localised deductions have failed. Our next step is
             * to resort to global deduction based on the total bomb count. This is computationally expensive compared
             * to any of the above deductions, which is why we only ever do it when all else fails, so that hopefully it
             * won't have to happen too often. Start by scanning the grid the see how many bombs and unknown squares are
             * left.
             */
            int hiddenSquares = 0;
            int unknownMines = this.board.getBombCount();
            for (Type type : this.boardKnowledge) {
                if (type == Type.SOLVER_HIDDEN) hiddenSquares++;
                else if (type == Type.BOMB) unknownMines--;
            }
            if (hiddenSquares == 0) {
                // We have revealed all squares we have solved the board
                return 0;
            }
            // Other simple cases, remaining squares are all bombs or clear.
            if (unknownMines == 0 || unknownMines == hiddenSquares) {
                int x = 0;
                int y = 0;
                for (Type type : this.boardKnowledge) {
                    if (type == Type.SOLVER_HIDDEN) {
                        this.revealSquare(new Pos2i(x, y), unknownMines == hiddenSquares);
                    }

                    x++;
                    if (x == this.board.size()) {
                        x = 0;
                        y++;
                    }
                }
            }

            // Brute force remaining bomb layouts
            int[] allSets = this.setStore.getAllSets();
            List<Integer> partitions = this.partitionSets(allSets);
            int partitionStart = 0;
            int unseenSquares = hiddenSquares;

            List<TreeMap<Integer, Map<Integer, Integer>>> partitionBruteForcedMines = new ArrayList<>(
                    partitions.size());
            int minBruteForcedMines = 0;
            int maxBruteForcedMines = 0;
            for (int partitionEnd : partitions) {
                Set<Integer> partitionIndices = this.findAvailableIndices(allSets, partitionStart, partitionEnd);
                unseenSquares -= partitionIndices.size();
                TreeMap<Integer, Map<Integer, Integer>> bruteForced = new TreeMap<>();
                // A map of placed numBombs -> partitionIndex -> CanBeMine or CanBeClear (2 bits)
                this.bruteForce(
                        bruteForced,
                        allSets,
                        partitionStart,
                        partitionEnd,
                        unknownMines,
                        unseenSquares,
                        0,
                        new ArrayList<>(partitionIndices));
                partitionStart = partitionEnd;
                partitionBruteForcedMines.add(bruteForced);
                maxBruteForcedMines += bruteForced.lastKey();
                minBruteForcedMines += bruteForced.firstKey();
            }
            int minPlaceableMines = Math.max(0, unknownMines - unseenSquares);
            int maxPlaceableMines = unknownMines;
            int[] knownSetMines = new int[1];
            Map<Integer, Integer> prunedBruteForcedMines = this.pruneBruteForcedMines(
                    partitionBruteForcedMines,
                    minPlaceableMines,
                    maxPlaceableMines,
                    minBruteForcedMines,
                    maxBruteForcedMines,
                    knownSetMines);
            for (Map.Entry<Integer, Integer> entry : prunedBruteForcedMines.entrySet()) {
                if (entry.getValue() != 0b11) {
                    int index = entry.getKey();
                    this.revealSquare(this.board.toPos(index), entry.getValue() == 0b10);
                }
            }

            if (knownSetMines[0] != -1
                    && (knownSetMines[0] == unknownMines || unknownMines - knownSetMines[0] == unseenSquares)) {
                // The partitions all have a known bomb count which equals the bombs we have to place or
                // leave all unseen as bombs.
                // So all other cells must be safe to reveal

                for (int i = 0; i < boardKnowledge.length; i++) {
                    if (this.boardKnowledge[i] == Type.SOLVER_HIDDEN && !prunedBruteForcedMines.containsKey(i)) {
                        this.revealSquare(this.board.toPos(i), knownSetMines[0] != unknownMines);
                    }
                }

            }
            if (!this.squaresTodo.isEmpty()) {
                // Revealing a square adds it to the do list
                // Use the knowledge gained from brute forcing to attempt more local deductions
                continue;
            }

            // Brute force analysis could not find any safe squares, so perturb the underlying grid
            // to provide further logic. Do so by either filling or emptying a set from setStore
            // We may have no sets at this point; there are 2+ unknown squares walled off by bombs so no clue reaches
            // them
            List<Pos2i> cellsToChange = new ArrayList<>();
            int backtrackDepth = 4;
            if (this.setStore.size() == 0) {
                // We have no sets so backtrack until we can perturb the grid to be solvable
                // TODO check if this produces unusually large clusters of bombs. May need to instead break the wall of
                // bombs
                for (int i = 0; i < this.boardKnowledge.length; i++) {
                    if (this.boardKnowledge[i] == Type.SOLVER_HIDDEN) cellsToChange.add(this.board.toPos(i));
                }
                backtrackDepth <<= 1;
                this.backtrack(backtrackDepth);
            } else {
                int set = this.setStore.getRandomSet();
                int x = LogicMineSet.getSetX(set);
                int y = LogicMineSet.getSetY(set);
                for (int bit : LogicMineSet.iterateSetMask(set)) {
                    cellsToChange.add(new Pos2i(x + bit % 3, y + bit / 3));
                }
            }
            List<Integer> changedOtherCells = this.board.perturbBombLocations(cellsToChange, this.boardKnowledge);
            // The perturb may fail to fill or empty the provided cells to change,
            // eg not enough bombs or safe squares outside the cells to change
            while (changedOtherCells == null) {
                // In that case backtrack further in logic to provide more bombs or safe cells
                this.backtrack(backtrackDepth);
                backtrackDepth <<= 1;
                changedOtherCells = this.board.perturbBombLocations(cellsToChange, this.boardKnowledge);
            }

            // If we have to backtrack then the mask's of the sets will not correlate with the
            // boundary of known and unknown tiles, so rebuild all the sets.
            if (backtrackDepth != 4) this.invalidateSets();

            // The board returns a list of negative numbers if it filled cellsToChange with bombs
            boolean isCellsToChangeBomb = changedOtherCells.get(0) < 0;
            // Apply the perturb's changes to the solver's knowledge
            if (isCellsToChangeBomb) {
                for (int i : changedOtherCells) this.applyPerturb(~i, +1);
                for (Pos2i pos : cellsToChange) this.applyPerturb(this.board.toIndex(pos), -1);
            } else {
                for (int i : changedOtherCells) this.applyPerturb(i, -1);
                for (Pos2i pos : cellsToChange) this.applyPerturb(this.board.toIndex(pos), 1);
            }
            // Having perturbed the grid to generate logic continue solving
            continue;

        }

        return -1;
    }

    private void applyPerturb(int index, int bombDiff) {
        Pos2i pos = this.board.toPos(index);
        byte adjacentMines = 0;
        for (int x = Math.max(0, pos.getX() - 1); x < Math.min(pos.getX() + 1, this.board.size() - 1); x++) {
            for (int y = Math.max(0, pos.getY() - 1); y < Math.min(pos.getY() + 1, this.board.size() - 1); y++) {
                int ii = this.board.toIndex(new Pos2i(x, y));
                Type type = this.boardKnowledge[ii];
                if (type == Type.BOMB) adjacentMines++;
                else if (type != Type.SOLVER_HIDDEN) {
                    this.boardKnowledge[ii] = Type.byId((byte) (type.getId() + bombDiff));
                }
            }
        }
        this.boardKnowledge[index] = bombDiff == 1 ? Type.BOMB : Type.byId((byte) (adjacentMines - 1));

    }

    private void bruteForce(Map<Integer, Map<Integer, Integer>> res, int[] sets, int start, int end, int unknownBombs,
            int otherSquares, int bombsPlaced, List<Integer> partitionIndices) {
        // If we have run out of bombs to place or places to put them, this isn't a valid combination
        if (unknownBombs < 0 || unknownBombs > otherSquares) return;
        // Base case, we have found a satisfying filling of the partition's sets.
        if (start == end) {
            // Go through the grid and mark each cell as either bomb or clear
            Map<Integer, Integer> cellPossibilities = res.containsKey(bombsPlaced) ? res.get(bombsPlaced)
                    : new TreeMap<>();
            if (cellPossibilities.isEmpty()) {
                for (int i : partitionIndices) {
                    cellPossibilities.put(i, 0);
                }
            }
            for (int i : partitionIndices) {
                Type type = this.boardKnowledge[i];

                int currentPossibility = cellPossibilities.get(i);
                int newPossibility = currentPossibility | (type == Type.BOMB ? 0b10 : 0b01);
                if (newPossibility != currentPossibility) cellPossibilities.put(i, newPossibility);
            }
        } else {
            // Otherwise assign all combinations of filling the bomb in the set
            int set = sets[start];
            int bombsToPlace = LogicMineSet.getSetBombs(set);
            int availableCells = LogicMineSet.getMaskSquareCount(set);
            int x = LogicMineSet.getSetX(set);
            int y = LogicMineSet.getSetY(set);
            int[] setGridIndices = new int[availableCells];

            int setii = 0;
            for (int bit : LogicMineSet.iterateSetMask(set)) {
                int x_ = x + bit % 3;
                int y_ = y + bit / 3;
                int i = x_ + this.board.size() * y_;
                setGridIndices[setii] = i;
                setii++;
                if (this.boardKnowledge[i] == Type.BOMB) bombsToPlace--;
            }
            if (bombsToPlace < 0) return; // Another set(s)'s bombs have overfilled this set

            for (List<Integer> bombSpots : LogicMineSet.permutations(availableCells, bombsToPlace)) {
                for (int sGIi : bombSpots) {
                    this.boardKnowledge[setGridIndices[sGIi]] = Type.BOMB;
                }
                bruteForce(
                        res,
                        sets,
                        start + 1,
                        end,
                        unknownBombs - bombsToPlace,
                        otherSquares,
                        bombsPlaced + bombsToPlace,
                        partitionIndices);
                for (int sGIi : bombSpots) {
                    this.boardKnowledge[setGridIndices[sGIi]] = Type.SOLVER_HIDDEN;
                }

            }

        }

    }

    private Map<Integer, Integer> pruneBruteForcedMines(List<TreeMap<Integer, Map<Integer, Integer>>> partitions,
            int minPlace, int maxPlace, int minBrute, int maxBrute, int[] knownSetMines) {
        TreeMap<Integer, Integer> res = new TreeMap<>();

        if (minPlace > minBrute || maxBrute > maxPlace) {
            boolean pruned = false;
            int loops = 20;
            do {
                for (TreeMap<Integer, Map<Integer, Integer>> p : partitions) {
                    minBrute -= p.firstKey();
                    maxBrute -= p.lastKey();

                    int lowKey = -1;
                    int highKey = -1;
                    for (int bombs : p.keySet()) {
                        if (bombs + maxBrute < minPlace) {
                            lowKey = bombs;
                        } else break;
                    }
                    for (int bombs : p.descendingKeySet()) {
                        if (bombs + minBrute > maxPlace) {
                            highKey = bombs;
                        }
                    }
                    pruned = pruned || lowKey != -1 || highKey != -1;
                    if (lowKey != -1) p.headMap(lowKey).clear();
                    if (highKey != -1) p.tailMap(highKey).clear();
                    minBrute += p.firstKey();
                    maxBrute += p.lastKey();
                }
            } while (pruned && (loops-- >= 0));
        }
        knownSetMines[0] = 0;
        for (Map<Integer, Map<Integer, Integer>> p : partitions) {
            if (knownSetMines[0] != -1 && p.size() == 1) {
                knownSetMines[0] += (int) p.keySet().toArray()[0];
            } else knownSetMines[0] = -1;
            for (Map<Integer, Integer> posToBits : p.values()) {
                for (Map.Entry<Integer, Integer> posBits : posToBits.entrySet()) {
                    int pos = posBits.getKey();
                    int bits = posBits.getValue();
                    if (res.containsKey(pos)) {
                        int oldBits = res.get(pos);
                        int newBits = bits | oldBits;
                        if (newBits != oldBits) res.put(pos, newBits);
                    } else {
                        res.put(pos, bits);
                    }
                }
            }
        }
        return res;
    }

    private void backtrack(int depth) {
        for (depth = Math.min(depth, revealStack.size()); depth > 0; depth--) {
            Pos2i pos = revealStack.remove(revealStack.size() - 1);
            this.boardKnowledge[this.board.toIndex(pos)] = Type.SOLVER_HIDDEN;
        }
    }

    private void invalidateSets() {
        this.setStore = new MineSets();

    }
}
