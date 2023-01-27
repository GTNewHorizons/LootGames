package com.jamesswafford.chess4j.board;

import com.jamesswafford.chess4j.Color;
import com.jamesswafford.chess4j.board.squares.*;
import com.jamesswafford.chess4j.pieces.*;
import java.util.ArrayList;
import java.util.List;

public final class MoveGen {

    private MoveGen() {}

    private static void addMoves(Board board, Square fromSq, long moveMap, List<Move> moves) {
        while (moveMap != 0) {
            int toVal = Bitboard.lsb(moveMap);
            Square toSq = Square.valueOf(toVal);
            Piece toPiece = board.getPiece(toSq);
            moves.add(new Move(fromSq, toSq, toPiece));
            moveMap ^= Bitboard.squares[toVal];
        }
    }

    private static void addPawnMove(List<Move> moves, Square fromSq, Square toSq, Piece captured) {
        if (toSq.rank() == Rank.RANK_1 || toSq.rank() == Rank.RANK_8) {
            boolean isWhite = toSq.rank() == Rank.RANK_8;
            moves.add(new Move(fromSq, toSq, captured, isWhite ? Queen.WHITE_QUEEN : Queen.BLACK_QUEEN));
            moves.add(new Move(fromSq, toSq, captured, isWhite ? Rook.WHITE_ROOK : Rook.BLACK_ROOK));
            moves.add(new Move(fromSq, toSq, captured, isWhite ? Bishop.WHITE_BISHOP : Bishop.BLACK_BISHOP));
            moves.add(new Move(fromSq, toSq, captured, isWhite ? Knight.WHITE_KNIGHT : Knight.BLACK_KNIGHT));
        } else {
            moves.add(new Move(fromSq, toSq, captured));
        }
    }

    private static void genBishopMoves(Board board, List<Move> moves, boolean onlyCaps) {
        long pieceMap = board.getPlayerToMove() == Color.WHITE ? board.getWhiteBishops() : board.getBlackBishops();
        while (pieceMap != 0) {
            int sqVal = Bitboard.msb(pieceMap);
            long moveMap = Magic.getBishopMoves(board, sqVal, getTargetSquares(board, true, !onlyCaps));
            addMoves(board, Square.valueOf(sqVal), moveMap, moves);
            pieceMap ^= Bitboard.squares[sqVal];
        }
    }

    private static void genCastlingMoves(Board board, List<Move> moves) {
        Color player = board.getPlayerToMove();

        if (player.isWhite()) {
            Square fromSq = Square.valueOf(File.FILE_E, Rank.RANK_1);
            if (board.canCastle(CastlingRights.WHITE_KINGSIDE)) {
                moves.add(new Move(fromSq, Square.valueOf(File.FILE_G, Rank.RANK_1), null));
            }
            if (board.canCastle(CastlingRights.WHITE_QUEENSIDE)) {
                moves.add(new Move(fromSq, Square.valueOf(File.FILE_C, Rank.RANK_1), null));
            }
        } else {
            Square fromSq = Square.valueOf(File.FILE_E, Rank.RANK_8);
            if (board.canCastle(CastlingRights.BLACK_KINGSIDE)) {
                moves.add(new Move(fromSq, Square.valueOf(File.FILE_G, Rank.RANK_8), null));
            }
            if (board.canCastle(CastlingRights.BLACK_QUEENSIDE)) {
                moves.add(new Move(fromSq, Square.valueOf(File.FILE_C, Rank.RANK_8), null));
            }
        }
    }

    private static void genKingMoves(Board board, List<Move> moves, boolean onlyCaps) {
        Square fromSq = board.getKingSquare(board.getPlayerToMove());

        long moveMap = Bitboard.kingMoves[fromSq.value()] & getTargetSquares(board, true, !onlyCaps);
        addMoves(board, fromSq, moveMap, moves);

        if (!onlyCaps) {
            genCastlingMoves(board, moves);
        }
    }

    private static void genKnightMoves(Board board, List<Move> moves, boolean onlyCaps) {
        long pieceMap = board.getPlayerToMove() == Color.WHITE ? board.getWhiteKnights() : board.getBlackKnights();

        while (pieceMap != 0) {
            int sqVal = Bitboard.msb(pieceMap);
            long moveMap = Bitboard.knightMoves[sqVal] & getTargetSquares(board, true, !onlyCaps);
            addMoves(board, Square.valueOf(sqVal), moveMap, moves);
            pieceMap ^= Bitboard.squares[sqVal];
        }
    }

    public static List<Move> genLegalMoves(Board board) {
        List<Move> pseudoLegalMoves = genPseudoLegalMoves(board);
        List<Move> legalMoves = new ArrayList<Move>();

        for (Move m : pseudoLegalMoves) {
            board.applyMove(m);

            if (!board.isOpponentInCheck()) {
                legalMoves.add(m);
            }
            board.undoLastMove();
        }

        return legalMoves;
    }

    private static void genPawnMoves(Board board, List<Move> moves, boolean onlyCaps) {

        long allPieces = board.getWhitePieces() | board.getBlackPieces();
        long pmap;

        if (board.getPlayerToMove() == Color.WHITE) {
            long targets = board.getBlackPieces();
            if (board.getEPSquare() != null)
                targets |= Bitboard.squares[board.getEPSquare().value()];

            // attacks west
            pmap = ((board.getWhitePawns() & ~Bitboard.files[File.FILE_A.getValue()]) >> 9) & targets;
            while (pmap != 0) {
                int toSqVal = Bitboard.msb(pmap);
                Square toSq = Square.valueOf(toSqVal);
                Piece captured = toSq == board.getEPSquare() ? Pawn.BLACK_PAWN : board.getPiece(toSq);
                addPawnMove(moves, SouthEast.getInstance().next(toSq), toSq, captured);
                pmap ^= Bitboard.squares[toSqVal];
            }

            // attacks east
            pmap = ((board.getWhitePawns() & ~Bitboard.files[File.FILE_H.getValue()]) >> 7) & targets;
            while (pmap != 0) {
                int toSqVal = Bitboard.msb(pmap);
                Square toSq = Square.valueOf(toSqVal);
                Piece captured = toSq == board.getEPSquare() ? Pawn.BLACK_PAWN : board.getPiece(toSq);
                addPawnMove(moves, SouthWest.getInstance().next(toSq), toSq, captured);
                pmap ^= Bitboard.squares[toSqVal];
            }

            // push promotions
            pmap = ((board.getWhitePawns() & Bitboard.ranks[Rank.RANK_7.getValue()]) >> 8) & ~allPieces;
            while (pmap != 0) {
                int toSqVal = Bitboard.msb(pmap);
                Square toSq = Square.valueOf(toSqVal);
                addPawnMove(moves, South.getInstance().next(toSq), toSq, null);
                pmap ^= Bitboard.squares[toSqVal];
            }

            // pawn pushes less promotions
            if (!onlyCaps) {
                pmap = ((board.getWhitePawns() & ~Bitboard.ranks[Rank.RANK_7.getValue()]) >> 8) & ~allPieces;
                while (pmap != 0) {
                    int toSqVal = Bitboard.msb(pmap);
                    Square toSq = Square.valueOf(toSqVal);
                    moves.add(new Move(South.getInstance().next(toSq), toSq, null));
                    if (toSq.rank() == Rank.RANK_3
                            && board.getPiece(North.getInstance().next(toSq)) == null) {
                        moves.add(new Move(
                                South.getInstance().next(toSq),
                                North.getInstance().next(toSq),
                                null));
                    }
                    pmap ^= Bitboard.squares[toSqVal];
                }
            }
        } else {
            long targets = board.getWhitePieces();
            if (board.getEPSquare() != null)
                targets |= Bitboard.squares[board.getEPSquare().value()];

            // attacks west
            pmap = ((board.getBlackPawns() & ~Bitboard.files[File.FILE_A.getValue()]) << 7) & targets;
            while (pmap != 0) {
                int toSqVal = Bitboard.lsb(pmap);
                Square toSq = Square.valueOf(toSqVal);
                Piece captured = toSq == board.getEPSquare() ? Pawn.WHITE_PAWN : board.getPiece(toSq);
                addPawnMove(moves, NorthEast.getInstance().next(toSq), toSq, captured);
                pmap ^= Bitboard.squares[toSqVal];
            }

            // attacks east
            pmap = ((board.getBlackPawns() & ~Bitboard.files[File.FILE_H.getValue()]) << 9) & targets;
            while (pmap != 0) {
                int toSqVal = Bitboard.lsb(pmap);
                Square toSq = Square.valueOf(toSqVal);
                Piece captured = toSq == board.getEPSquare() ? Pawn.WHITE_PAWN : board.getPiece(toSq);
                addPawnMove(moves, NorthWest.getInstance().next(toSq), toSq, captured);
                pmap ^= Bitboard.squares[toSqVal];
            }

            // push promotions
            pmap = ((board.getBlackPawns() & Bitboard.ranks[Rank.RANK_2.getValue()]) << 8) & ~allPieces;
            while (pmap != 0) {
                int toSqVal = Bitboard.lsb(pmap);
                Square toSq = Square.valueOf(toSqVal);
                addPawnMove(moves, North.getInstance().next(toSq), toSq, null);
                pmap ^= Bitboard.squares[toSqVal];
            }

            // pawn pushes less promotions
            if (!onlyCaps) {
                pmap = ((board.getBlackPawns() & ~Bitboard.ranks[Rank.RANK_2.getValue()]) << 8) & ~allPieces;
                while (pmap != 0) {
                    int toSqVal = Bitboard.lsb(pmap);
                    Square toSq = Square.valueOf(toSqVal);
                    moves.add(new Move(North.getInstance().next(toSq), toSq, null));
                    if (toSq.rank() == Rank.RANK_6
                            && board.getPiece(South.getInstance().next(toSq)) == null) {
                        moves.add(new Move(
                                North.getInstance().next(toSq),
                                South.getInstance().next(toSq),
                                null));
                    }
                    pmap ^= Bitboard.squares[toSqVal];
                }
            }
        }
    }

    public static List<Move> genPseudoLegalMoves(Board board) {
        return genPseudoLegalMoves(board, false);
    }

    public static List<Move> genPseudoLegalMoves(Board board, boolean onlyCapsPromos) {
        List<Move> moves = new ArrayList<Move>(100);

        genPawnMoves(board, moves, onlyCapsPromos);
        genKnightMoves(board, moves, onlyCapsPromos);
        genBishopMoves(board, moves, onlyCapsPromos);
        genRookMoves(board, moves, onlyCapsPromos);
        genQueenMoves(board, moves, onlyCapsPromos);
        genKingMoves(board, moves, onlyCapsPromos);

        return moves;
    }

    private static void genQueenMoves(Board board, List<Move> moves, boolean onlyCaps) {
        long pieceMap = board.getPlayerToMove() == Color.WHITE ? board.getWhiteQueens() : board.getBlackQueens();

        while (pieceMap != 0) {
            int sqVal = Bitboard.msb(pieceMap);
            long moveMap = Magic.getQueenMoves(board, sqVal, getTargetSquares(board, true, !onlyCaps));
            addMoves(board, Square.valueOf(sqVal), moveMap, moves);
            pieceMap ^= Bitboard.squares[sqVal];
        }
    }

    private static void genRookMoves(Board board, List<Move> moves, boolean onlyCaps) {
        long pieceMap = board.getPlayerToMove() == Color.WHITE ? board.getWhiteRooks() : board.getBlackRooks();

        while (pieceMap != 0) {
            int sqVal = Bitboard.msb(pieceMap);
            long moveMap = Magic.getRookMoves(board, sqVal, getTargetSquares(board, true, !onlyCaps));
            addMoves(board, Square.valueOf(sqVal), moveMap, moves);
            pieceMap ^= Bitboard.squares[sqVal];
        }
    }

    private static long getTargetSquares(Board board, boolean caps, boolean noncaps) {
        long targets = 0;

        if (caps) {
            targets = board.getPlayerToMove() == Color.WHITE ? board.getBlackPieces() : board.getWhitePieces();
        }

        if (noncaps) {
            targets |= ~(board.getWhitePieces() | board.getBlackPieces());
        }

        return targets;
    }
}
