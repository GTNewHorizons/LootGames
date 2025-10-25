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

    @Getter @Setter
    public int[][] solution = new int[SIZE][SIZE]; // Complete solution
    public int[][] puzzle = new int[SIZE][SIZE];   // Puzzle after removing cells
    public int[][] player = new int[SIZE][SIZE];   // Player's filled values

    public boolean[][] usedRows = new boolean[SIZE][SIZE+1];
    public boolean[][] usedCols = new boolean[SIZE][SIZE+1];
    public boolean[][] usedBlocks = new boolean[SIZE][SIZE+1];

    public static ICodec<Integer, NBTTagCompound> INT_CODEC = new ICodec<>() {
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

    public void generate(int blanks) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j <= SIZE; j++) {
                usedRows[i][j] = false;
                usedCols[i][j] = false;
                usedBlocks[i][j] = false;
            }

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                solution[i][j] = 0;

        fillSolution(0,0);
        copySolutionToPuzzle();
        removeCells(blanks);
        resetPlayer();
    }

    public int blockIndex(int r, int c) {
        return (r/3)*3 + (c/3);
    }

    public boolean fillSolution(int row, int col) {
        if (row == SIZE) return true;
        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = (col + 1) % SIZE;

        int[] nums = new int[]{1,2,3,4,5,6,7,8,9};
        shuffleArray(nums);

        for (int n : nums) {
            int b = blockIndex(row,col);
            if (!usedRows[row][n] && !usedCols[col][n] && !usedBlocks[b][n]) {
                solution[row][col] = n;
                usedRows[row][n] = true;
                usedCols[col][n] = true;
                usedBlocks[b][n] = true;

                if (fillSolution(nextRow,nextCol)) return true;

                solution[row][col] = 0;
                usedRows[row][n] = false;
                usedCols[col][n] = false;
                usedBlocks[b][n] = false;
            }
        }
        return false;
    }

    public void shuffleArray(int[] arr) {
        for (int i = arr.length-1; i>0; i--) {
            int j = RandHelper.RAND.nextInt(i+1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    public void copySolutionToPuzzle() {
        for (int i = 0; i < SIZE; i++)
            System.arraycopy(solution[i], 0, puzzle[i], 0, SIZE);
    }

    public void removeCells(int blanks) {
        List<Integer> pos = RandHelper.shuffledList(0, SIZE * SIZE - 1);
        int removed = 0;

        for (int idx : pos) {
            int r = idx / SIZE;
            int c = idx % SIZE;
            if (puzzle[r][c] == 0) continue;

            int backup = puzzle[r][c];
            puzzle[r][c] = 0;

            if (countSolutions(0,0,0) != 1) {
                puzzle[r][c] = backup;
            } else {
                removed++;
                if (removed >= blanks) break;
            }
        }
    }

    public int countSolutions(int row, int col, int total) {
        if (total > 1) return total;
        if (row == SIZE) return total+1;

        int nextRow = col == SIZE-1 ? row+1 : row;
        int nextCol = (col+1)%SIZE;

        if (puzzle[row][col] != 0)
            return countSolutions(nextRow, nextCol, total);

        for (int n = 1; n <= 9; n++) {
            if (canPlaceTemp(row,col,n)) {
                puzzle[row][col] = n;
                usedRows[row][n] = true;
                usedCols[col][n] = true;
                usedBlocks[blockIndex(row,col)][n] = true;

                total = countSolutions(nextRow,nextCol,total);

                puzzle[row][col] = 0;
                usedRows[row][n] = false;
                usedCols[col][n] = false;
                usedBlocks[blockIndex(row,col)][n] = false;

                if (total > 1) return total;
            }
        }
        return total;
    }

    public boolean canPlaceTemp(int r, int c, int n) {
        int b = blockIndex(r,c);
        return !usedRows[r][n] && !usedCols[c][n] && !usedBlocks[b][n];
    }

    public void resetPlayer() {
        for (int i = 0; i < SIZE; i++)
            System.arraycopy(puzzle[i], 0, player[i], 0, SIZE);
    }

    public boolean isGenerated() {
        return solution[0][0] != 0;
    }

    public int getPlayerValue(int x, int y) {
        return player[x][y];
    }

    public int getPlayerValue(Pos2i pos) {
        return getPlayerValue(pos.getX(), pos.getY());
    }

    public int getSolutionValue(int x, int y) {
        return solution[x][y];
    }

    public int getSolutionValue(Pos2i pos) {
        return getSolutionValue(pos.getX(), pos.getY());
    }

    public int getPuzzleValue(int x, int y) {
        return puzzle[x][y];
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
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (player[i][j] == 0 || player[i][j] != solution[i][j])
                    return false;
        return true;
    }

    public NBTTagCompound writeNBT() {
        NBTTagCompound t = new NBTTagCompound();
        t.setTag("puzzle", CodecUtils.write2DimArr(boxIntArray(puzzle), INT_CODEC));
        t.setTag("player", CodecUtils.write2DimArr(boxIntArray(player), INT_CODEC));
        t.setTag("solution", CodecUtils.write2DimArr(boxIntArray(solution), INT_CODEC));
        return t;
    }

    public void readNBT(NBTTagCompound t) {
        Integer[][] puzzleBox = CodecUtils.read2DimArr(t.getCompoundTag("puzzle"), Integer.class, INT_CODEC);
        Integer[][] playerBox = CodecUtils.read2DimArr(t.getCompoundTag("player"), Integer.class, INT_CODEC);
        Integer[][] solutionBox = CodecUtils.read2DimArr(t.getCompoundTag("solution"), Integer.class, INT_CODEC);

        puzzle = unboxIntArray(puzzleBox);
        player = unboxIntArray(playerBox);
        solution = unboxIntArray(solutionBox);
    }

    public void cSetPlayerValue(Pos2i pos, int value) {
        int r = pos.getX(), c = pos.getY();
        if (puzzle[r][c] == 0) {
            player[r][c] = value;
        }
    }

    public int countTotalBlanks() {
        int count = 0;
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (puzzle[i][j] == 0)
                    count++;
        return count;
    }

    public int countFilledCells() {
        int count = 0;
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (puzzle[i][j] == 0 && player[i][j] != 0)
                    count++;
        return count;
    }

    public static Integer[][] boxIntArray(int[][] arr) {
        Integer[][] boxed = new Integer[arr.length][];
        for (int i = 0; i < arr.length; i++) {
            boxed[i] = new Integer[arr[i].length];
            for (int j = 0; j < arr[i].length; j++) {
                boxed[i][j] = arr[i][j];
            }
        }
        return boxed;
    }

    public static int[][] unboxIntArray(Integer[][] arr) {
        int[][] unboxed = new int[arr.length][];
        for (int i = 0; i < arr.length; i++) {
            unboxed[i] = new int[arr[i].length];
            for (int j = 0; j < arr[i].length; j++) {
                unboxed[i][j] = arr[i][j] != null ? arr[i][j] : 0;
            }
        }
        return unboxed;
    }
}
