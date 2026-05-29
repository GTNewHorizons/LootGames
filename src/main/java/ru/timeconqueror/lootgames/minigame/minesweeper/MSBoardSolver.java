package ru.timeconqueror.lootgames.minigame.minesweeper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import ru.timeconqueror.lootgames.LootGames;
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

    private final MSBoard board;
    private final ArrayList<Pos2i> squaresTodo;
    private final Type[] boardKnowledge;
    private final Stack<LinkedList<Pos2i>> deductionStack;
    private LinkedList<Pos2i> currentDeduction;
    private final MineSets setStore;
    private Pos2i startPos;

    public MSBoardSolver(MSBoard board) {
        if (board.size() > 125) {
            // The set store may not find range queries correctly on x over the 127-129 range,
            // as the bit vector changes sign.
            // Also it may take too long to solve boards over 8 chunks in size.
            throw new IllegalArgumentException("Cannot solve too large of a Minesweeper Board.");
        }
        this.board = board;
        this.squaresTodo = new ArrayList<>(board.size() * board.size());
        this.boardKnowledge = new Type[board.size() * board.size()];
        for (int i = 0; i < board.size() * board.size(); i++) this.boardKnowledge[i] = Type.SOLVER_HIDDEN;
        this.deductionStack = new Stack<>();
        this.currentDeduction = new LinkedList<>();
        this.setStore = new MineSets();
    }

    private void addSquareToDo(Pos2i index) {
        this.squaresTodo.add(index);
    }

    private void endDeduction() {
        if (!currentDeduction.isEmpty()) {
            deductionStack.push(currentDeduction);
            this.currentDeduction = new LinkedList<>();
        }
    }

    private void revealSquares(int set, boolean isMines) {
        revealSquares(set, isMines, true);
    }

    private void revealSquares(int set, boolean isMine, boolean isFullDeduction) {
        int bit = 0b1;
        int setMask = LogicMineSet.getSetMask(set);
        if (setMask == 0) return;
        int setX = LogicMineSet.getSetX(set);
        int setY = LogicMineSet.getSetY(set);
        for (int dy = 0; dy < 3; dy++) {
            for (int dx = 0; dx < 3; dx++) {
                if ((setMask & bit) != 0) {
                    this.revealSquare(new Pos2i(setX + dx, setY + dy), isMine);
                }
                bit <<= 1;
            }
        }
        if (isFullDeduction) endDeduction();
    }

    private void revealSquare(Pos2i pos, boolean isMine) {
        this.currentDeduction.add(pos);
        Type revealedSquare;
        if (isMine) {
            revealedSquare = Type.BOMB;
        } else {
            revealedSquare = this.board.getType(pos);
        }
        LootGames.LOGGER.trace("Revealing square {} to be {}", pos, revealedSquare);
        this.boardKnowledge[this.board.toIndex(pos)] = revealedSquare;
        this.addSquareToDo(pos);
    }

    private Type getKnownField(Pos2i pos) {
        return this.boardKnowledge[this.board.toIndex(pos)];
    }

    private void processTodoSquares() {
        for (int i = 0; i < this.squaresTodo.size(); i++) {
            Pos2i pos = this.squaresTodo.get(i);
            Type cellType = this.getKnownField(pos);
            boolean isRevealedMine = cellType == Type.BOMB;
            if (!isRevealedMine) {

                // Create a bomb set around this square
                byte bombs = cellType.getId();
                int mask = 0;
                int bit = 0b1;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (!(pos.getX() + dx < 0 || pos.getX() + dx >= this.board.size()
                                || pos.getY() + dy < 0
                                || pos.getY() + dy >= this.board.size())) {
                            Type adjField = this.getKnownField(pos.add(dx, dy));
                            if (adjField == Type.SOLVER_HIDDEN) {
                                mask = mask | bit;
                            } else if (adjField == Type.BOMB) {
                                bombs--;
                            }
                        }
                        bit <<= 1;

                    }
                }
                // Check if the set around this square has unknown cells
                if (mask != 0) {
                    // Check if it is trivially known, to process immediately
                    int set = LogicMineSet.Set(pos.getX() - 1, pos.getY() - 1, mask, bombs);
                    if (bombs == 0 || bombs == LogicMineSet.getMaskSquareCount(mask)) {
                        this.revealSquares(set, bombs != 0);
                    } else {
                        // Otherwise store it for set munge / brute force analysis
                        this.setStore.addSet(set);
                    }
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
        while (this.setStore.hasTodo()) {
            int todoSet = this.setStore.getTodo();
            // Check if the set has no bombs, or bombs == bitCount(mask)
            // which are trivially known squares
            byte todoBombs = LogicMineSet.getSetBombs(todoSet);

            int todoSquares = LogicMineSet.getMaskSquareCount(LogicMineSet.getSetMask(todoSet));
            if (todoBombs == 0 || todoBombs == todoSquares) {
                this.revealSquares(todoSet, todoBombs == todoSquares);
                foundLogic = true;
                break;
            }
            // Otherwise find all overlapping sets and attempt deductions
            List<Integer> overlappingSets = this.setStore.setOverlap(todoSet);
            for (int otherSet : overlappingSets) {
                // Find the non overlapping parts of otherSet and todoSet
                int wing1 = this.setStore.setMunge(todoSet, otherSet, true);
                int wing2 = this.setStore.setMunge(otherSet, todoSet, true);
                int wing1Squares = LogicMineSet.getMaskSquareCount(wing1);
                int wing2Squares = LogicMineSet.getMaskSquareCount(wing2);
                int otherBombs = LogicMineSet.getSetBombs(otherSet);

                // Check if the cardinality of one set's wing is the same as the difference between the sets bombs
                // then the wing of the set with more bombs must be full, and the other wing empty
                // Eg 1?3 must be 1?3
                // . ????? . . . __?XX
                if ((wing1Squares == (todoBombs - otherBombs)) || (wing2Squares == (otherBombs - todoBombs))) {
                    boolean isWing1Mines = wing1Squares == todoBombs - otherBombs;
                    revealSquares(LogicMineSet.setSetMask(todoSet, wing1), isWing1Mines, false);
                    revealSquares(LogicMineSet.setSetMask(otherSet, wing2), !isWing1Mines);
                    foundLogic = true;
                    continue;
                }

                // Otherwise check if one set is a subset of the other.
                if (wing1Squares == 0 && wing2Squares != 0) {
                    // setTodo is a subset of otherSet
                    this.setStore.addSet(LogicMineSet.setSetMask(otherSet, wing2) - todoBombs);
                    foundLogic = true;
                } else if (wing2Squares == 0 && wing1Squares != 0) {
                    // otherSet is a subset of setTodo
                    this.setStore.addSet(LogicMineSet.setSetMask(todoSet, wing1) - otherBombs);
                    foundLogic = true;
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
            for (int bit : LogicMineSet.iterateSetMask(set)) {
                int x_ = x + bit % 3;
                int y_ = y + bit / 3;
                res.add(x_ + (board.size() * y_));
            }
        }
        return res;
    }

    public boolean isStarted() {
        return this.startPos != null;
    }

    public void startSolve(Pos2i startFieldPos) {
        if (this.startPos != null) {
            throw new IllegalStateException("Cannot start an already started solver");
        }
        this.startPos = startFieldPos;
        if (!this.board.isGenerated()) this.board.generate(startFieldPos);
        this.revealSquare(startFieldPos, false);
        endDeduction();
    }

    public int solveStep() {
        boolean haveSquares = !squaresTodo.isEmpty();
        this.processTodoSquares();
        boolean haveSets = this.setStore.hasTodo();
        this.processTodoSets();
        if (haveSets || haveSquares) {
            return 1;
        }
        return this.slowLogic();
    }

    public int slowLogic() {
        LootGames.LOGGER.trace("Performing slow logic checks");
        int hiddenSquares = 0;
        int unknownMines = this.board.getBombCount();
        for (Type type : this.boardKnowledge) {
            if (type == Type.SOLVER_HIDDEN) hiddenSquares++;
            else if (type == Type.BOMB) unknownMines--;
        }
        if (hiddenSquares == 0) {
            LootGames.LOGGER.trace("We have revealed all squares");
            // We have revealed all squares we have solved the board
            return 0;
        }
        // Other simple cases, remaining squares are all bombs or clear.
        if (unknownMines == 0 || unknownMines == hiddenSquares) {
            LootGames.LOGGER.trace("All hidden squares are either clear or mine");
            // We could reveal the squares in the solver and then return from the above check, but we only need know if
            // the board is solvable, so we return immediately
            return 0;
        }

        if (this.bruteForce(unknownMines, hiddenSquares)) return 2;

        this.perturbBoard();

        return 3;

    }

    public int solve(Pos2i startFieldPos) {
        if (!this.board.isGenerated()) {
            this.board.generate(startFieldPos);
        }
        this.revealSquare(startFieldPos, false);
        endDeduction();
        for (int loops = 0; loops < 500; loops++) {

            int logicResult = solveStep();
            if (logicResult == 0) return 0; // We solved the board
            // Else we found logic (1 or 2) or had to perturb the board (3)

        }
        return -1;
    }

    public boolean bruteForce(int unknownMines, int hiddenSquares) {
        LootGames.LOGGER.info("Starting brute force logic for minesweeper.");
        // Brute force remaining bomb layouts
        if (setStore.size() == 0) return false;
        int[] allSets = this.setStore.getAllSets();
        List<Integer> partitions = this.partitionSets(allSets);
        int partitionStart = 0;
        int unseenSquares = hiddenSquares;

        List<TreeMap<Integer, Map<Integer, Integer>>> partitionBruteForcedMines = new ArrayList<>(partitions.size());
        int minBruteForcedMines = 0;
        int maxBruteForcedMines = 0;
        for (int partitionEnd : partitions) {
            Set<Integer> partitionIndices = this.findAvailableIndices(allSets, partitionStart, partitionEnd);
            unseenSquares -= partitionIndices.size();
            TreeMap<Integer, Map<Integer, Integer>> bruteForced = new TreeMap<>();
            // A map of placed numBombs -> partitionIndex -> CanBeMine or CanBeClear (2 bits)
            this.bruteForceRecursion(
                    bruteForced,
                    allSets,
                    partitionStart,
                    partitionEnd,
                    unknownMines,
                    unseenSquares,
                    0,
                    new ArrayList<>(partitionIndices));
            LootGames.LOGGER.trace("Brute force found the following map {}", bruteForced);
            if (bruteForced.isEmpty()) {
                throw new IllegalStateException("Brute force analysis could not find any satisfying mine assignment");
            }
            unseenSquares += partitionIndices.size();
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
        // Tell caller if we found any logic
        LootGames.LOGGER.trace("Brute force found {} squares to reveal", squaresTodo.size());
        endDeduction();
        return !this.squaresTodo.isEmpty();
    }

    private void bruteForceRecursion(Map<Integer, Map<Integer, Integer>> res, int[] sets, int start, int end,
            int unknownBombs, int otherSquares, int bombsPlaced, List<Integer> partitionIndices) {
        // If we have run out of bombs to place or places to put them, this isn't a valid combination
        // Base case, we have found a satisfying filling of the partition's sets.
        if (start == end) {
            if (unknownBombs < 0 || unknownBombs > otherSquares) return;
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
            res.put(bombsPlaced, cellPossibilities);
        } else {
            // Otherwise assign all combinations of filling the bomb in the set
            int set = sets[start];
            int bombsToPlace = LogicMineSet.getSetBombs(set);
            int availableCells = LogicMineSet.getMaskSquareCount(LogicMineSet.getSetMask(set));
            int x = LogicMineSet.getSetX(set);
            int y = LogicMineSet.getSetY(set);
            List<Integer> setGridIndices = new ArrayList<>(availableCells);

            for (int bit : LogicMineSet.iterateSetMask(set)) {
                int x_ = x + bit % 3;
                int y_ = y + bit / 3;
                int i = x_ + this.board.size() * y_;
                if (this.boardKnowledge[i] == Type.BOMB) bombsToPlace--;
                else if (this.boardKnowledge[i] == Type.SOLVER_HIDDEN) {
                    setGridIndices.add(i);
                }
            }
            if (bombsToPlace < 0 || unknownBombs - bombsToPlace < 0 || bombsToPlace > setGridIndices.size()) {
                return; // Another set(s)'s bombs have overfilled this set, or we have run out of mines to fill this set
            }

            for (int setGridIndex : setGridIndices) {
                this.boardKnowledge[setGridIndex] = Type.EMPTY;
            }
            for (List<Integer> bombSpots : LogicMineSet.permutations(setGridIndices.size(), bombsToPlace)) {
                for (int sGIi : bombSpots) {
                    this.boardKnowledge[setGridIndices.get(sGIi)] = Type.BOMB;
                }
                bruteForceRecursion(
                        res,
                        sets,
                        start + 1,
                        end,
                        unknownBombs - bombsToPlace,
                        otherSquares,
                        bombsPlaced + bombsToPlace,
                        partitionIndices);
                for (int sGIi : bombSpots) {
                    this.boardKnowledge[setGridIndices.get(sGIi)] = Type.EMPTY;
                }

            }
            for (int setGridIndex : setGridIndices) {
                this.boardKnowledge[setGridIndex] = Type.SOLVER_HIDDEN;
            }

        }

    }

    /**
     * Combine the partitions produced by brute forcing the mines. Prune off any solutions with a bomb count that is
     * infeasible. Eg: 4 partitions have bomb counts [4,5,6,7,8], [1,2], [1], [1] with max, min placeable being 8, 0
     * From inspection, the first partition cannot have 6,7 or 8 bombs, as that would require placing more bombs than
     * are available
     * 
     * @param knownSetMines a pointer to tell the caller if the number of mines required to place in the partitions is
     *                      known exactly
     * @return
     */
    private Map<Integer, Integer> pruneBruteForcedMines(List<TreeMap<Integer, Map<Integer, Integer>>> partitions,
            int minPlace, int maxPlace, int minBrute, int maxBrute, int[] knownSetMines) {
        TreeMap<Integer, Integer> res = new TreeMap<>();

        if (minPlace > minBrute || maxBrute > maxPlace) {
            boolean pruned;
            int loops = 20;
            do {
                pruned = false;
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
                        } else break;
                    }
                    pruned = pruned || lowKey != -1 || highKey != -1;
                    if (lowKey != -1) p.headMap(lowKey, true).clear();
                    if (highKey != -1) p.tailMap(highKey).clear();
                    minBrute += p.firstKey();
                    maxBrute += p.lastKey();
                }
            } while (pruned && (loops-- >= 0));
        }
        // We have pruned all partition solutions with infeasible bomb counts
        // Combine the partition solutions into a single map of index -> bomb | clear | unknown; see call of
        // bruteForceRecursion in bruteForce
        knownSetMines[0] = 0;
        for (Map<Integer, Map<Integer, Integer>> p : partitions) {
            if (knownSetMines[0] != -1 && p.size() == 1) {
                // Check if the solution to the partition has a known number of bombs
                knownSetMines[0] += (int) p.keySet().toArray()[0];
            } else knownSetMines[0] = -1; // If any are unknown, then the sum is unknown
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

    public void perturbBoard() {
        // Brute force analysis could not find any safe squares, so perturb the underlying grid
        // to provide further logic. Do so by either filling or emptying a set from setStore
        // We may have no sets at this point; there are 2+ unknown squares walled off by bombs so no clue reaches
        // them
        List<Pos2i> cellsToChange = new ArrayList<>();
        int initialBacktrackDepth = 4;
        int backtrackDepth = initialBacktrackDepth;
        if (this.setStore.size() == 0) {
            // We have no sets so backtrack until we can perturb the grid to be solvable
            // TODO check if this produces unusually large clusters of bombs. May need to instead break the wall of
            // bombs
            LootGames.LOGGER.trace("Perturbing entire board");
            for (int i = 0; i < this.boardKnowledge.length; i++) {
                if (this.boardKnowledge[i] == Type.SOLVER_HIDDEN) cellsToChange.add(this.board.toPos(i));
            }
            backtrackDepth <<= 1;
            this.backtrack(backtrackDepth);
        } else {
            int set = this.setStore.getRandomSet();
            LootGames.LOGGER.trace("Perturbing {}", LogicMineSet.toString(set));
            int x = LogicMineSet.getSetX(set);
            int y = LogicMineSet.getSetY(set);
            for (int bit : LogicMineSet.iterateSetMask(set)) {
                cellsToChange.add(new Pos2i(x + bit % 3, y + bit / 3));
            }
        }
        List<BombPerturbation> changedOtherCells = this.board.perturbBombLocations(cellsToChange, this.boardKnowledge);
        // The perturb may fail to fill or empty the provided cells to change,
        // eg not enough bombs or safe squares outside the cells to change
        while (changedOtherCells == null) {
            LootGames.LOGGER.trace("Board failed to perturb, backtracking {} deductions.", backtrackDepth);
            // In that case backtrack further in logic to provide more bombs or safe cells
            this.backtrack(backtrackDepth);
            backtrackDepth <<= 1;
            changedOtherCells = this.board.perturbBombLocations(cellsToChange, this.boardKnowledge);
        }

        // If we have to backtrack then the mask's of the sets will not correlate with the
        // boundary of known and unknown tiles, we need to rebuild all the sets.
        // Initially clear the current sets to make the perturb applications more efficient
        // (SetOverlap is quicker on empty tree)
        if (backtrackDepth != initialBacktrackDepth) this.setStore.clear();

        // The board returns a list of negative numbers if it filled cellsToChange with bombs
        // Apply the perturb's changes to the solver's knowledge
        LootGames.LOGGER.trace("Applying {} perturbations", changedOtherCells.size());
        for (BombPerturbation bp : changedOtherCells) {
            this.applyPerturb(bp);
        }
        board.applyPerturbations(changedOtherCells);
        LootGames.LOGGER.trace(boardKnowledgeToString());
        if (backtrackDepth != initialBacktrackDepth) this.rebuildSets();
    }

    private void applyPerturb(BombPerturbation bp) {
        byte adjacentMines = 0;
        for (int x = Math.max(0, bp.x - 1); x <= Math.min(bp.x + 1, this.board.size() - 1); x++) {
            for (int y = Math.max(0, bp.y - 1); y <= Math.min(bp.y + 1, this.board.size() - 1); y++) {
                int ii = this.board.toIndex(new Pos2i(x, y));
                Type type = this.boardKnowledge[ii];
                if (type == Type.BOMB) adjacentMines++;
                else if (type != Type.SOLVER_HIDDEN) {
                    this.boardKnowledge[ii] = Type.byId((byte) (type.getId() + bp.bombDiff));
                }
            }
        }
        int index = bp.x + bp.y * this.board.size();
        this.boardKnowledge[index] = this.boardKnowledge[index] == Type.SOLVER_HIDDEN ? Type.SOLVER_HIDDEN
                : bp.bombDiff == 1 ? Type.BOMB : Type.byId((byte) (adjacentMines - 1));

        // Update all the sets containing the perturbation
        List<Integer> affectedSets = setStore.setOverlap(bp.x, bp.y);
        for (int set : affectedSets) {
            setStore.removeSet(set);
            setStore.addSet(set + (int) bp.bombDiff);
        }
    }

    private void backtrack(int depth) {
        for (depth = Math.min(depth, deductionStack.size()); depth > 0; depth--) {
            LinkedList<Pos2i> poses = deductionStack.pop();
            for (Pos2i pos : poses) {
                this.boardKnowledge[this.board.toIndex(pos)] = Type.SOLVER_HIDDEN;
            }
        }
    }

    private void rebuildSets() {
        int x = 0;
        int y = 0;
        for (Type cell : this.boardKnowledge) {
            if (cell.getId() > Type.EMPTY.getId()) {
                this.addSquareToDo(new Pos2i(x, y));
            }
            if (++x == board.size()) {
                x = 0;
                y++;
            }
        }
    }

    public String boardKnowledgeToString() {
        String typeStrings = "?X 12345678";
        StringBuilder builder = new StringBuilder(board.size() * board.size() + board.size());
        int i = 0;
        for (int y = 0; y < board.size(); y++) {
            builder.append('\n');
            for (int x = 0; x < board.size(); x++) {
                Type type = boardKnowledge[i];
                builder.append(typeStrings.charAt(type.getId() + 2));
                i++;
            }
        }
        return builder.toString();
    }

    public String deductionsToString() {
        return deductionStack.toString();
    }
}
