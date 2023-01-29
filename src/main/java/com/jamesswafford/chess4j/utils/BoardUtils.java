package com.jamesswafford.chess4j.utils;

import java.util.List;

import com.jamesswafford.chess4j.Color;
import com.jamesswafford.chess4j.board.Board;
import com.jamesswafford.chess4j.board.squares.Square;
import com.jamesswafford.chess4j.pieces.*;

public class BoardUtils {

    public static int getNumPawns(Board board, Color sideToMove) {
        int numPawns = 0;

        List<Square> squares = Square.allSquares();
        for (Square sq : squares) {
            Piece p = board.getPiece(sq);
            if (p instanceof Pawn && p.getColor().equals(sideToMove)) {
                numPawns++;
            }
        }

        return numPawns;
    }

    public static int getNumNonPawns(Board board, Color sideToMove) {
        int n = 0;

        List<Square> squares = Square.allSquares();
        for (Square sq : squares) {
            Piece p = board.getPiece(sq);
            if ((p instanceof Queen || p instanceof Rook || p instanceof Bishop || p instanceof Knight)
                    && p.getColor().equals(sideToMove)) {
                n++;
            }
        }

        return n;
    }

    public static boolean isDiagonal(Square sq1, Square sq2) {
        return sq1.rank().distance(sq2.rank()) == sq1.file().distance(sq2.file());
    }
}
