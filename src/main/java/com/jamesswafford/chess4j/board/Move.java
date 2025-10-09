package com.jamesswafford.chess4j.board;

import com.jamesswafford.chess4j.board.squares.Square;
import com.jamesswafford.chess4j.pieces.Piece;

public class Move {

    private Square from, to;
    private Piece captured, promotion;

    public Move(Square from, Square to) {
        this.from = from;
        this.to = to;
    }

    public Move(Square from, Square to, Piece captured) {
        this(from, to);
        this.captured = captured;
    }

    public Move(Square from, Square to, Piece captured, Piece promotion) {
        this(from, to, captured);
        this.promotion = promotion;
    }

    public Piece captured() {
        return captured;
    }

    public Square from() {
        return from;
    }

    public Piece promotion() {
        return promotion;
    }

    public Square to() {
        return to;
    }

    @Override
    public String toString() {
        String s = from.toString() + to.toString();
        if (promotion != null) {
            s += "=" + promotion.toString();
        }
        return s;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Move)) {
            return false;
        }
        Move mv = (Move) obj;
        if (!mv.from()
            .equals(this.from())) {
            return false;
        }
        if (!mv.to()
            .equals(this.to())) {
            return false;
        }
        if (mv.captured() == null) {
            if (this.captured() != null) {
                return false;
            }
        } else {
            if (!mv.captured()
                .equals(this.captured())) {
                return false;
            }
        }
        if (mv.promotion() == null) {
            return this.promotion() == null;
        } else {
            return mv.promotion()
                .equals(this.promotion());
        }
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 13 + from.hashCode();
        hash = hash * 17 + to.hashCode();
        hash = hash * 31 + (captured == null ? 0 : captured.hashCode());
        hash = hash * 37 + (promotion == null ? 0 : promotion.hashCode());
        return hash;
    }
}
