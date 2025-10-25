package ru.timeconqueror.lootgames.minigame.sudoku;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.utils.future.ICodec;
import ru.timeconqueror.timecore.api.util.CodecUtils;
import ru.timeconqueror.timecore.api.util.RandHelper;

import java.util.List;

public class SudokuBoard {
    public static final int SIZE = 9;
    @Getter
    @Setter
    public Integer[][] solution = new Integer[SIZE][SIZE]; // Complete solution
    public Integer[][] puzzle = new Integer[SIZE][SIZE]; // Puzzle after removing cells
    public Integer[][] player = new Integer[SIZE][SIZE]; // Player's filled values

    public static final ICodec<Integer, NBTTagCompound> INT_CODEC = new ICodec<>() {

        @Override
        public NBTTagCompound encode(Integer obj) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("v", obj);
            return tag;
        }

        @Override
        public Integer decode(NBTTagCompound nbtTag) {
            return nbtTag.getInteger("v");
        }
    };

    /** Generate a Sudoku board based on the number of blanks */
    public void generate(int blanks) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                solution[i][j] = 0;
            }
        }

        fillSolution(0, 0);
        copySolutionToPuzzle();
        removeCells(blanks);
        resetPlayer();
    }

    public boolean fillSolution(int row, int col) {
        if (row == SIZE) return true;
        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = (col + 1) % SIZE;
        List<Integer> nums = RandHelper.shuffledList(1, 9);
        for (int n : nums) {
            if (canPlace(solution, row, col, n)) {
                solution[row][col] = n;
                if (fillSolution(nextRow, nextCol)) return true;
                solution[row][col] = 0;
            }
        }
        return false;
    }

    public boolean canPlace(Integer[][] g, int r, int c, int n) {
        for (int i = 0; i < SIZE; i++) if (g[r][i] == n || g[i][c] == n) return false;
        int br = (r / 3) * 3, bc = (c / 3) * 3;
        for (int rr = br; rr < br + 3; rr++) for (int cc = bc; cc < bc + 3; cc++) if (g[rr][cc] == n) return false;
        return true;
    }

    public void copySolutionToPuzzle() {
        for (int i = 0; i < SIZE; i++) System.arraycopy(solution[i], 0, puzzle[i], 0, SIZE);
    }

    public void removeCells(int blanks) {
        List<Integer> pos = RandHelper.shuffledList(0, SIZE * SIZE - 1);
        int removed = 0;

        for (int idx : pos) {
            int r = idx / SIZE;
            int c = idx % SIZE;

            int backup = puzzle[r][c];
            if (backup == 0) continue; // Skip if already empty

            puzzle[r][c] = 0;

            // Create a deep copy of the current puzzle state for the solver
            Integer[][] boardCopy = new Integer[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++) {
                System.arraycopy(puzzle[i], 0, boardCopy[i], 0, SIZE);
            }

            // Check if the puzzle has a unique solution
            if (countSolutions(boardCopy, 0, 0) != 1) {
                // Restore, this cell cannot be removed
                puzzle[r][c] = backup;
            } else {
                removed++;
                if (removed >= blanks) break;
            }
        }
    }

    public int countSolutions(Integer[][] board, int row, int col) {
        if (row == SIZE) return 1;

        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = (col + 1) % SIZE;

        if (board[row][col] != null && board[row][col] != 0) {
            return countSolutions(board, nextRow, nextCol);
        }

        int totalSolutions = 0;
        for (int n = 1; n <= 9; n++) {
            if (canPlace(board, row, col, n)) {
                board[row][col] = n;
                totalSolutions += countSolutions(board, nextRow, nextCol);
                if (totalSolutions > 1) {
                    board[row][col] = 0;
                    return totalSolutions;
                }
                board[row][col] = 0;
            }
        }
        return totalSolutions;
    }

    public void resetPlayer() {
        for (int i = 0; i < SIZE; i++) System.arraycopy(puzzle[i], 0, player[i], 0, SIZE);
    }

    public boolean isGenerated() {
        return solution[0][0] != 0;
    }

    public int getPlayerValue(int x, int y) {
        Integer v = player[x][y];
        return v != null ? v : 0;
    }

    public int getPlayerValue(Pos2i pos) {
        return getPlayerValue(pos.getX(), pos.getY());
    }

    public int getSolutionValue(int x, int y) {
        Integer v = solution[x][y];
        return v != null ? v : 0;
    }

    public int getSolutionValue(Pos2i pos) {
        return getSolutionValue(pos.getX(), pos.getY());
    }

    public int getPuzzleValue(int x, int y) {
        Integer v = puzzle[x][y];
        return v != null ? v : 0;
    }

    public int getPuzzleValue(Pos2i pos) {
        return getPuzzleValue(pos.getX(), pos.getY());
    }

    public void cycleValueMinus(Pos2i pos) {
        int r = pos.getX(), c = pos.getY();
        if (puzzle[r][c] != 0) return;
        player[r][c] = (player[r][c] + 9) % 10;
    }

    public void cycleValueAdd(Pos2i pos) {
        int r = pos.getX(), c = pos.getY();
        if (puzzle[r][c] != 0) return;
        player[r][c] = (player[r][c] + 1) % 10;
    }

    public boolean checkWin() {
        for (int i = 0; i < SIZE; i++) for (int j = 0; j < SIZE; j++) {
            if (player[i][j] == null || player[i][j] == 0 || !player[i][j].equals(solution[i][j])) return false;
        }
        return true;
    }

    public NBTTagCompound writeNBT() {
        NBTTagCompound t = new NBTTagCompound();
        t.setTag("puzzle", CodecUtils.write2DimArr(puzzle, INT_CODEC));
        t.setTag("player", CodecUtils.write2DimArr(player, INT_CODEC));
        t.setTag("solution", CodecUtils.write2DimArr(solution, INT_CODEC));
        return t;
    }

    public void readNBT(NBTTagCompound t) {
        puzzle = CodecUtils.read2DimArr(t.getCompoundTag("puzzle"), Integer.class, INT_CODEC);
        player = CodecUtils.read2DimArr(t.getCompoundTag("player"), Integer.class, INT_CODEC);
        solution = CodecUtils.read2DimArr(t.getCompoundTag("solution"), Integer.class, INT_CODEC);
    }

    public void cSetPlayerValue(Pos2i pos, int value) {
        int r = pos.getX(), c = pos.getY();

        if (puzzle[r][c] == 0) {
            player[r][c] = value;
        }
    }

    public int countTotalBlanks() {
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (puzzle[i][j] != null && puzzle[i][j] == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public int countFilledCells() {
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (puzzle[i][j] != null && puzzle[i][j] == 0 && player[i][j] != null && player[i][j] != 0) {
                    count++;
                }
            }
        }
        return count;
    }
}
