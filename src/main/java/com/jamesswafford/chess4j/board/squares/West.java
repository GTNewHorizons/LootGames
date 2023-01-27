package com.jamesswafford.chess4j.board.squares;

public final class West extends Direction {

    private static final West INSTANCE = new West();

    private West() {}

    public static West getInstance() {
        return INSTANCE;
    }

    @Override
    public Square next(Square sq) {
        return Square.valueOf(sq.file().west(), sq.rank());
    }

    @Override
    public boolean isDiagonal() {
        return false;
    }

    @Override
    public int value() {
        return 6;
    }
}
