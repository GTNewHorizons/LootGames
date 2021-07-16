package ru.timeconqueror.lootgames.minigame.minesweeper;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import ru.timeconqueror.lootgames.api.util.Pos2i;
import ru.timeconqueror.lootgames.utils.future.ICodec;
import ru.timeconqueror.timecore.api.util.CodecUtils;
import ru.timeconqueror.timecore.api.util.Wrapper;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MSBoard {
    int cFlaggedFields;
    private MSField[][] board;
    private int size;
    private int bombCount;

    public MSBoard(int size, int bombCount) {
        this.size = size;
        this.bombCount = bombCount;
    }

    void updateFlaggedFields_c() {
        cFlaggedFields = 0;

        for (MSField[] msFields : board) {
            for (MSField msField : msFields) {
                if (msField.mark == Mark.FLAG) {
                    cFlaggedFields++;
                }
            }
        }
    }

    public boolean isGenerated() {
        return board != null;
    }

    void reveal(Pos2i pos) {
        getField(pos).reveal();
    }

    void reveal(int x, int y) {
        getField(x, y).reveal();
    }

    public boolean isHidden(Pos2i pos) {
        return getField(pos).isHidden;
    }

    public boolean isHidden(int x, int y) {
        return getField(x, y).isHidden;
    }

    public Type getType(Pos2i pos) {
        return getField(pos).type;
    }

    public Type getType(int x, int y) {
        return getField(x, y).type;
    }

    void swapMark(Pos2i pos) {
        getField(pos).swapMark();
    }

    public Mark getMark(Pos2i pos) {
        return getField(pos).mark;
    }

    public Mark getMark(int x, int y) {
        return getField(x, y).mark;
    }

    void resetBoard() {
        resetBoard(size, bombCount);
    }

    void resetBoard(int newBoardSize, int newBombCount) {
        size = newBoardSize;
        bombCount = newBombCount;

        board = null;
    }

    boolean isBomb(int x, int y) {
        return getType(x, y) == Type.BOMB;
    }

    boolean isBomb(Pos2i pos) {
        return getType(pos) == Type.BOMB;
    }

    public boolean hasFieldOn(Pos2i pos) {
        return pos.getX() >= 0 && pos.getY() >= 0 && pos.getX() < size && pos.getY() < size;
    }

    boolean checkWin() {
        boolean winState = true;

        loop:
        for (MSField[] msFields : board) {
            for (MSField msField : msFields) {
                if (msField.type == Type.BOMB) {
                    if (!msField.isHidden || msField.mark != Mark.FLAG) {
                        winState = false;
                        break loop;
                    }
                } else {
                    if (msField.isHidden) {
                        winState = false;
                        break loop;
                    }
                }
            }
        }

        return winState;
    }

    public MSField getField(Pos2i pos) {
        return board[pos.getX()][pos.getY()];
    }

    private MSField getField(int x, int y) {
        return board[x][y];
    }

    private List<Integer> getAvailableBombIndices(Pos2i start, int fieldSize) {
        int emptyPlaces = size * size - bombCount;

        if (emptyPlaces < 0)
            throw new IllegalStateException("How is that possible, that board square is less or equal to bomb count? Square = " + (size * size) + ", bomb count = " + bombCount);

        int safeDistance;
        if (emptyPlaces >= 13) {
            safeDistance = 2;
        } else if (emptyPlaces >= 9) {
            safeDistance = 1;
        } else {
            safeDistance = 0;
        }

        Wrapper<Integer> count = new Wrapper<>(0);

        return IntStream.range(0, fieldSize).filter(index -> {
            if (count.get() > emptyPlaces) {
                return true;
            }

            Pos2i pos = toPos(index);//TODO optimize

            if (start.manhattanDistanceTo(pos) <= safeDistance) {
                count.set(count.get() + 1);
                return false;
            } else {
                return true;
            }
        }).boxed().collect(Collectors.toList());
    }

    public void generate(Pos2i startFieldPos) {
        if (toIndex(startFieldPos) > (size * size) - 1) {
            throw new IllegalArgumentException(String.format("Start Pos must be strictly less than Board size. Current values: start pos = %1$s, boardSize = %2$d", startFieldPos, size));
        }

        board = new MSField[size][size];
        int square = size * size;

        //adding bombs
        List<Integer> fields = getAvailableBombIndices(startFieldPos, square);

        Collections.shuffle(fields);

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = new MSField(null, true, Mark.NO_MARK);
            }
        }

        for (Integer integer : fields.subList(0, bombCount)) {
            board[integer % size][integer / size].type = Type.BOMB;
        }

        //adding numbers and spaces
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (!isBomb(i, j)) {
                    board[i][j].type = Type.byId((byte) getConnectedBombCount(i, j));
                }
            }
        }
    }

    /**
     * Converts field index to pos.
     */
    public Pos2i toPos(int fieldIndex) {
        return new Pos2i(fieldIndex % size, fieldIndex / size);
    }

    /**
     * Converts pos to field index.
     * <p>Example:
     * <p>Pos {x = 2; y = 4}, gridSize = 5 -> index 22 out of (gridSize * gridSize)
     */
    public int toIndex(Pos2i pos) {
        return pos.getY() * size + pos.getX();
    }

    /**
     * Returns count of bombs that were stood right next to given field.
     */
    private int getConnectedBombCount(int x, int y) {
        int bombCount = 0;
        for (int i = -1; i <= 1; i++) {

            int xCoord = x + i;
            if (xCoord >= 0 && xCoord < size()) {
                for (int j = -1; j <= 1; j++) {

                    int yCoord = y + j;
                    if (yCoord >= 0 && yCoord < size()) {
                        if (isBomb(xCoord, yCoord)) {
                            bombCount++;
                        }
                    }

                }
            }
        }

        return bombCount;
    }

    public void forEach(Consumer<Pos2i> func) {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                func.accept(new Pos2i(x, y));
            }
        }
    }

    public void forEach(BiConsumer<Integer, Integer> func) {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                func.accept(x, y);
            }
        }
    }

    public void cSetField(Pos2i pos, MSField field) {
        MSField oldField = board[pos.getX()][pos.getY()];
        Mark oldMark = oldField.mark;

        if (!field.isHidden) field.mark = Mark.NO_MARK;

        board[pos.getX()][pos.getY()] = field;


        if (oldMark == Mark.FLAG && field.mark != Mark.FLAG) {
            cFlaggedFields--;
        } else if (oldMark != Mark.FLAG && field.mark == Mark.FLAG) {
            cFlaggedFields++;
        }
    }

    private void setBoard(MSField[][] board) {
        this.board = board;
    }

    public int size() {
        return size;
    }

    public int getBombCount() {
        return bombCount;
    }

    void setBombCount(int bombCount) {
        this.bombCount = bombCount;
    }

    public int cGetFlaggedField() {
        return cFlaggedFields;
    }

    public void cSetFlaggedFields(int cFlaggedFields) {
        this.cFlaggedFields = cFlaggedFields;
    }

    void setSize(int size) {
        this.size = size;
    }

    NBTTagCompound writeNBTForSaving() {
        return CodecUtils.write2DimArr(board, MSField.SAVE_CODEC);
    }

    NBTTagCompound writeNBTForClient() {
        return CodecUtils.write2DimArr(board, MSField.SYNC_CODEC);
    }

    void readNBTFromClient(NBTTagCompound boardTag) {
        setBoard(CodecUtils.read2DimArr(boardTag, MSField.class, MSField.SYNC_CODEC));
        updateFlaggedFields_c();
    }

    void readNBTFromSave(NBTTagCompound boardTag) {
        setBoard(CodecUtils.read2DimArr(boardTag, MSField.class, MSField.SAVE_CODEC));
    }

    public static class MSField {
        private Type type;
        private Mark mark;
        private boolean isHidden;

        MSField(Type type, boolean isHidden, Mark mark) {
            this.type = type;
            this.isHidden = isHidden;
            this.mark = mark;
        }

        private void swapMark() {
            mark = mark.getNext();
        }

        private void resetMark() {
            mark = Mark.NO_MARK;
        }

        private void reveal() {
            isHidden = false;
            resetMark();
        }

        public Type getType() {
            return type;
        }

        public Mark getMark() {
            return mark;
        }

        public boolean isHidden() {
            return isHidden;
        }

        @Override
        public String toString() {
            return "Field{type: " + type + ", hidden: " + isHidden + ", mark: " + mark + "}";
        }

        public static final Codec SYNC_CODEC = new Codec(true);
        public static final Codec SAVE_CODEC = new Codec(false);

        public static class Codec implements ICodec<MSField, NBTTagCompound> {
            private final boolean forSync;

            public Codec(boolean forSync) {
                this.forSync = forSync;
            }

            @Override
            public NBTTagCompound encode(MSField field) {
                NBTTagCompound compound = new NBTTagCompound();
                if (!forSync || !field.isHidden()) {
                    compound.setTag("type", Type.CODEC.encode(field.getType()));
                }

                compound.setBoolean("hidden", field.isHidden());
                compound.setTag("mark", Mark.CODEC.encode(field.getMark()));
                return compound;
            }

            @Override
            public MSField decode(NBTTagCompound nbt) {
                return new MSField(
                        forSync && !nbt.hasKey("type") ? Type.EMPTY : Type.CODEC.decode((NBTTagByte) nbt.getTag("type")),
                        nbt.getBoolean("hidden"),
                        Mark.CODEC.decode((NBTTagByte) nbt.getTag("mark"))
                );
            }
        }
    }
}
