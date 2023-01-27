package com.jamesswafford.chess4j.board;

import com.jamesswafford.chess4j.Color;
import com.jamesswafford.chess4j.board.squares.*;
import com.jamesswafford.chess4j.hash.Zobrist;
import com.jamesswafford.chess4j.pieces.*;
import java.util.*;

public final class Board {

    public static final Board INSTANCE = new Board();

    private List<Undo> undoStack = new ArrayList<Undo>();
    private Map<Square, Piece> pieceMap = new HashMap<Square, Piece>();
    private Map<Piece, Integer> pieceCountsMap = new HashMap<Piece, Integer>();
    private MyCastlingRights castlingRights = new MyCastlingRights();
    private Color playerToMove;
    private Square epSquare;
    private Square whiteKingSquare, blackKingSquare;
    private int moveCounter;
    private int fiftyCounter;
    private long whitePawns, blackPawns;
    private long whiteKnights, blackKnights;
    private long whiteBishops, blackBishops;
    private long whiteRooks, blackRooks;
    private long whiteQueens, blackQueens;
    private long whitePieces, blackPieces;
    private long zobristKey;
    private long pawnKey;

    private Board() {
        resetBoard();
    }

    public void addCastlingRight(CastlingRights castlingRight) {
        if (castlingRight == CastlingRights.WHITE_KINGSIDE) {
            if (!castlingRights.isWhiteKingside()) {
                castlingRights.setWhiteKingside();
                zobristKey ^= Zobrist.getCastlingKey(castlingRight);
            }
        } else if (castlingRight == CastlingRights.WHITE_QUEENSIDE) {
            if (!castlingRights.isWhiteQueenside()) {
                castlingRights.setWhiteQueenside();
                zobristKey ^= Zobrist.getCastlingKey(castlingRight);
            }
        } else if (castlingRight == CastlingRights.BLACK_KINGSIDE) {
            if (!castlingRights.isBlackKingside()) {
                castlingRights.setBlackKingside();
                zobristKey ^= Zobrist.getCastlingKey(castlingRight);
            }
        } else if (castlingRight == CastlingRights.BLACK_QUEENSIDE) {
            if (!castlingRights.isBlackQueenside()) {
                castlingRights.setBlackQueenside();
                zobristKey ^= Zobrist.getCastlingKey(castlingRight);
            }
        } else {
            throw new IllegalArgumentException("illegal castling right: " + castlingRight);
        }
    }

    public void addPiece(Piece p, Square s) {
        if (p != null) {
            pieceMap.put(s, p);
            pieceCountsMap.put(p, pieceCountsMap.get(p) + 1);
            zobristKey ^= Zobrist.getPieceKey(s, p);

            long bb = Bitboard.squares[s.value()];
            if (p.isWhite()) {
                whitePieces |= bb;
                if (p == Pawn.WHITE_PAWN) {
                    whitePawns |= bb;
                    pawnKey ^= Zobrist.getPieceKey(s, p);
                } else if (p == Knight.WHITE_KNIGHT) {
                    whiteKnights |= bb;
                } else if (p == Bishop.WHITE_BISHOP) {
                    whiteBishops |= bb;
                } else if (p == Rook.WHITE_ROOK) {
                    whiteRooks |= bb;
                } else if (p == Queen.WHITE_QUEEN) {
                    whiteQueens |= bb;
                }
            } else {
                blackPieces |= bb;
                if (p == Pawn.BLACK_PAWN) {
                    blackPawns |= bb;
                    pawnKey ^= Zobrist.getPieceKey(s, p);
                } else if (p == Knight.BLACK_KNIGHT) {
                    blackKnights |= bb;
                } else if (p == Bishop.BLACK_BISHOP) {
                    blackBishops |= bb;
                } else if (p == Rook.BLACK_ROOK) {
                    blackRooks |= bb;
                } else if (p == Queen.BLACK_QUEEN) {
                    blackQueens |= bb;
                }
            }
        }
    }

    private void applyKingSpecialCases(Move m) {
        if (playerToMove.isWhite()) {
            whiteKingSquare = m.to();
            clearCastlingRight(CastlingRights.WHITE_KINGSIDE);
            clearCastlingRight(CastlingRights.WHITE_QUEENSIDE);
            if (m.from().equals(Square.valueOf(File.FILE_E, Rank.RANK_1))) {
                if (m.to().equals(Square.valueOf(File.FILE_G, Rank.RANK_1))) {
                    fiftyCounter = 0;
                    movePiece(Square.valueOf(File.FILE_H, Rank.RANK_1), Square.valueOf(File.FILE_F, Rank.RANK_1));
                } else if (m.to().equals(Square.valueOf(File.FILE_C, Rank.RANK_1))) {
                    fiftyCounter = 0;
                    movePiece(Square.valueOf(File.FILE_A, Rank.RANK_1), Square.valueOf(File.FILE_D, Rank.RANK_1));
                }
            }
        } else {
            blackKingSquare = m.to();
            clearCastlingRight(CastlingRights.BLACK_KINGSIDE);
            clearCastlingRight(CastlingRights.BLACK_QUEENSIDE);
            if (m.from().equals(Square.valueOf(File.FILE_E, Rank.RANK_8))) {
                if (m.to().equals(Square.valueOf(File.FILE_G, Rank.RANK_8))) {
                    fiftyCounter = 0;
                    movePiece(Square.valueOf(File.FILE_H, Rank.RANK_8), Square.valueOf(File.FILE_F, Rank.RANK_8));
                } else if (m.to().equals(Square.valueOf(File.FILE_C, Rank.RANK_8))) {
                    fiftyCounter = 0;
                    movePiece(Square.valueOf(File.FILE_A, Rank.RANK_8), Square.valueOf(File.FILE_D, Rank.RANK_8));
                }
            }
        }
    }

    public void applyMove(Move m) {
        assert (verify());
        undoStack.add(new Undo(m, fiftyCounter, castlingRights.getValue(), epSquare, zobristKey));

        moveCounter++;
        if (m.captured() == null) {
            // note: fifty counter may still get set to 0
            fiftyCounter++;
        } else {
            fiftyCounter = 0;
            if (m.to().equals(Square.valueOf(File.FILE_H, Rank.RANK_1))) {
                clearCastlingRight(CastlingRights.WHITE_KINGSIDE);
            } else if (m.to().equals(Square.valueOf(File.FILE_A, Rank.RANK_1))) {
                clearCastlingRight(CastlingRights.WHITE_QUEENSIDE);
            }
            if (m.to().equals(Square.valueOf(File.FILE_H, Rank.RANK_8))) {
                clearCastlingRight(CastlingRights.BLACK_KINGSIDE);
            } else if (m.to().equals(Square.valueOf(File.FILE_A, Rank.RANK_8))) {
                clearCastlingRight(CastlingRights.BLACK_QUEENSIDE);
            }
        }

        Piece p = clearSquare(m.from());
        Square oldEPSquare = clearEPSquare();

        if (p instanceof Pawn) {
            applyPawnMove(p, m, oldEPSquare);
        } else {
            if (m.captured() != null) {
                clearSquare(m.to());
            }
            addPiece(p, m.to());
            if (p instanceof King) {
                applyKingSpecialCases(m);
            } else if (p instanceof Rook) {
                applyRookSpecialCases(m);
            }
        }

        swapPlayer();
        assert (verify());
    }

    private void applyPawnMove(Piece p, Move m, Square oldEPSquare) {
        fiftyCounter = 0;
        if (p.isWhite()) {
            if (m.from().rank().equals(Rank.RANK_2) && m.to().rank().equals(Rank.RANK_4)) {
                addPiece(p, m.to());
                epSquare = Square.valueOf(m.to().file(), m.to().rank().south());
                zobristKey ^= Zobrist.getEnPassantKey(epSquare);
            } else if (m.to().equals(oldEPSquare)) {
                clearSquare(
                        Square.valueOf(oldEPSquare.file(), oldEPSquare.rank().south()));
                addPiece(p, m.to());
            } else if (m.to().rank().equals(Rank.RANK_8)) {
                clearSquare(m.to());
                addPiece(m.promotion(), m.to());
            } else {
                clearSquare(m.to());
                addPiece(p, m.to());
            }
        } else {
            if (m.from().rank().equals(Rank.RANK_7) && m.to().rank().equals(Rank.RANK_5)) {
                addPiece(p, m.to());
                epSquare = Square.valueOf(m.to().file(), m.to().rank().north());
                zobristKey ^= Zobrist.getEnPassantKey(epSquare);
            } else if (m.to().equals(oldEPSquare)) {
                clearSquare(
                        Square.valueOf(oldEPSquare.file(), oldEPSquare.rank().north()));
                addPiece(p, m.to());
            } else if (m.to().rank().equals(Rank.RANK_1)) {
                clearSquare(m.to());
                addPiece(m.promotion(), m.to());
            } else {
                clearSquare(m.to());
                addPiece(p, m.to());
            }
        }
    }

    private void applyRookSpecialCases(Move m) {
        if (playerToMove.isWhite()) {
            if (m.from().equals(Square.valueOf(File.FILE_H, Rank.RANK_1))) {
                fiftyCounter = 0;
                clearCastlingRight(CastlingRights.WHITE_KINGSIDE);
            } else if (m.from().equals(Square.valueOf(File.FILE_A, Rank.RANK_1))) {
                fiftyCounter = 0;
                clearCastlingRight(CastlingRights.WHITE_QUEENSIDE);
            }
        } else {
            if (m.from().equals(Square.valueOf(File.FILE_H, Rank.RANK_8))) {
                fiftyCounter = 0;
                clearCastlingRight(CastlingRights.BLACK_KINGSIDE);
            } else if (m.from().equals(Square.valueOf(File.FILE_A, Rank.RANK_8))) {
                fiftyCounter = 0;
                clearCastlingRight(CastlingRights.BLACK_QUEENSIDE);
            }
        }
    }

    private boolean blackCanCastleKingSide() {
        if (!hasCastlingRight(CastlingRights.BLACK_KINGSIDE)) {
            return false;
        }
        boolean pathIsClear =
                isEmpty(Square.valueOf(File.FILE_F, Rank.RANK_8)) && isEmpty(Square.valueOf(File.FILE_G, Rank.RANK_8));
        if (!pathIsClear) {
            return false;
        }

        Color opponent = Color.swap(playerToMove);
        boolean wouldCrossCheck = AttackDetector.attacked(this, Square.valueOf(File.FILE_E, Rank.RANK_8), opponent)
                || AttackDetector.attacked(this, Square.valueOf(File.FILE_F, Rank.RANK_8), opponent);
        return !wouldCrossCheck;
    }

    private boolean blackCanCastleQueenSide() {
        if (!hasCastlingRight(CastlingRights.BLACK_QUEENSIDE)) {
            return false;
        }

        boolean pathIsClear = isEmpty(Square.valueOf(File.FILE_D, Rank.RANK_8))
                && isEmpty(Square.valueOf(File.FILE_C, Rank.RANK_8))
                && isEmpty(Square.valueOf(File.FILE_B, Rank.RANK_8));
        if (!pathIsClear) {
            return false;
        }

        Color opponent = Color.swap(playerToMove);
        boolean wouldCrossCheck = AttackDetector.attacked(this, Square.valueOf(File.FILE_E, Rank.RANK_8), opponent)
                || AttackDetector.attacked(this, Square.valueOf(File.FILE_D, Rank.RANK_8), opponent);
        return !wouldCrossCheck;
    }

    public boolean canCastle(CastlingRights cr) {

        if (cr.equals(CastlingRights.WHITE_KINGSIDE)) {
            return whiteCanCastleKingSide();
        } else if (cr.equals(CastlingRights.WHITE_QUEENSIDE)) {
            return whiteCanCastleQueenSide();
        } else if (cr.equals(CastlingRights.BLACK_KINGSIDE)) {
            return blackCanCastleKingSide();
        } else {
            return blackCanCastleQueenSide();
        }
    }

    public void clearBoard() {
        List<Square> squares = Square.allSquares();
        for (Square sq : squares) {
            clearSquare(sq);
        }

        clearEPSquare();
        Set<CastlingRights> crs = EnumSet.allOf(CastlingRights.class);
        for (CastlingRights cr : crs) {
            clearCastlingRight(cr);
        }
        fiftyCounter = 0;
        undoStack.clear();

        assert (pieceCountsMap.get(Pawn.WHITE_PAWN) == 0);
        assert (pieceCountsMap.get(Pawn.BLACK_PAWN) == 0);
        assert (pieceCountsMap.get(Queen.WHITE_QUEEN) == 0);
        assert (pieceCountsMap.get(Queen.BLACK_QUEEN) == 0);
        assert (pieceCountsMap.get(Rook.WHITE_ROOK) == 0);
        assert (pieceCountsMap.get(Rook.BLACK_ROOK) == 0);
        assert (pieceCountsMap.get(Knight.WHITE_KNIGHT) == 0);
        assert (pieceCountsMap.get(Knight.BLACK_KNIGHT) == 0);
        assert (pieceCountsMap.get(Bishop.WHITE_BISHOP) == 0);
        assert (pieceCountsMap.get(Bishop.BLACK_BISHOP) == 0);
    }

    public void clearCastlingRight(CastlingRights castlingRight) {
        if (castlingRight == CastlingRights.WHITE_KINGSIDE) {
            if (castlingRights.isWhiteKingside()) {
                castlingRights.removeWhiteKingside();
                zobristKey ^= Zobrist.getCastlingKey(castlingRight);
            }
        } else if (castlingRight == CastlingRights.WHITE_QUEENSIDE) {
            if (castlingRights.isWhiteQueenside()) {
                castlingRights.removeWhiteQueenside();
                zobristKey ^= Zobrist.getCastlingKey(castlingRight);
            }
        } else if (castlingRight == CastlingRights.BLACK_KINGSIDE) {
            if (castlingRights.isBlackKingside()) {
                castlingRights.removeBlackKingside();
                zobristKey ^= Zobrist.getCastlingKey(castlingRight);
            }
        } else if (castlingRight == CastlingRights.BLACK_QUEENSIDE) {
            if (castlingRights.isBlackQueenside()) {
                castlingRights.removeBlackQueenside();
                zobristKey ^= Zobrist.getCastlingKey(castlingRight);
            }
        } else {
            throw new IllegalArgumentException("illegal castling right: " + castlingRight);
        }
    }

    public Square clearEPSquare() {
        Square sq = epSquare;
        if (sq != null) {
            zobristKey ^= Zobrist.getEnPassantKey(sq);
            epSquare = null;
        }
        return sq;
    }

    private Piece clearSquare(Square s) {
        Piece p = getPiece(s);
        if (p != null) {
            pieceMap.remove(s);
            pieceCountsMap.put(p, pieceCountsMap.get(p) - 1);
            zobristKey ^= Zobrist.getPieceKey(s, p);
            long bb = Bitboard.squares[s.value()];
            if (p.isWhite()) {
                whitePieces ^= bb;
                if (p == Pawn.WHITE_PAWN) {
                    whitePawns ^= bb;
                    pawnKey ^= Zobrist.getPieceKey(s, p);
                } else if (p == Knight.WHITE_KNIGHT) {
                    whiteKnights ^= bb;
                } else if (p == Bishop.WHITE_BISHOP) {
                    whiteBishops ^= bb;
                } else if (p == Rook.WHITE_ROOK) {
                    whiteRooks ^= bb;
                } else if (p == Queen.WHITE_QUEEN) {
                    whiteQueens ^= bb;
                }
            } else {
                blackPieces ^= bb;
                if (p == Pawn.BLACK_PAWN) {
                    blackPawns ^= bb;
                    pawnKey ^= Zobrist.getPieceKey(s, p);
                } else if (p == Knight.BLACK_KNIGHT) {
                    blackKnights ^= bb;
                } else if (p == Bishop.BLACK_BISHOP) {
                    blackBishops ^= bb;
                } else if (p == Rook.BLACK_ROOK) {
                    blackRooks ^= bb;
                } else if (p == Queen.BLACK_QUEEN) {
                    blackQueens ^= bb;
                }
            }
        }
        return p;
    }

    public synchronized Board deepCopy() {
        Board b = new Board();
        b.undoStack.clear();
        b.undoStack.addAll(undoStack);
        b.pieceMap.clear();
        for (Square sq : pieceMap.keySet()) {
            b.pieceMap.put(sq, pieceMap.get(sq));
        }
        b.castlingRights.setValue(castlingRights.getValue());
        b.playerToMove = playerToMove;
        b.epSquare = epSquare;
        b.whiteKingSquare = whiteKingSquare;
        b.blackKingSquare = blackKingSquare;
        b.whitePawns = whitePawns;
        b.blackPawns = blackPawns;
        b.whiteKnights = whiteKnights;
        b.blackKnights = blackKnights;
        b.whiteBishops = whiteBishops;
        b.blackBishops = blackBishops;
        b.whiteRooks = whiteRooks;
        b.blackRooks = blackRooks;
        b.whiteQueens = whiteQueens;
        b.blackQueens = blackQueens;
        b.whitePieces = whitePieces;
        b.blackPieces = blackPieces;
        b.fiftyCounter = fiftyCounter;
        b.moveCounter = moveCounter;
        b.zobristKey = zobristKey;
        b.pawnKey = pawnKey;
        for (Piece p : pieceCountsMap.keySet()) {
            b.pieceCountsMap.put(p, pieceCountsMap.get(p));
        }
        return b;
    }

    public boolean equalExceptMoveHistory(Board otherBoard, boolean strict) {
        if (!this.pieceMap.equals(otherBoard.pieceMap)) {
            return false;
        }
        if (!this.pieceCountsMap.equals(otherBoard.pieceCountsMap)) {
            return false;
        }
        if (!this.castlingRights.equals(otherBoard.castlingRights)) {
            return false;
        }
        if (!this.getPlayerToMove().equals(otherBoard.getPlayerToMove())) {
            return false;
        }
        if (this.epSquare == null) {
            if (otherBoard.epSquare != null) {
                return false;
            }
        } else {
            if (!this.epSquare.equals(otherBoard.epSquare)) {
                return false;
            }
        }
        if (this.blackKingSquare == null) {
            if (otherBoard.blackKingSquare != null) {
                return false;
            }
        } else {
            if (!this.blackKingSquare.equals(otherBoard.blackKingSquare)) {
                return false;
            }
        }
        if (this.whiteKingSquare == null) {
            if (otherBoard.whiteKingSquare != null) {
                return false;
            }
        } else {
            if (!this.whiteKingSquare.equals(otherBoard.whiteKingSquare)) {
                return false;
            }
        }
        if (this.whitePawns != otherBoard.whitePawns) {
            return false;
        }
        if (this.blackPawns != otherBoard.blackPawns) {
            return false;
        }
        if (this.whiteKnights != otherBoard.whiteKnights) {
            return false;
        }
        if (this.blackKnights != otherBoard.blackKnights) {
            return false;
        }
        if (this.whiteBishops != otherBoard.whiteBishops) {
            return false;
        }
        if (this.blackBishops != otherBoard.blackBishops) {
            return false;
        }
        if (this.whiteRooks != otherBoard.whiteRooks) {
            return false;
        }
        if (this.blackRooks != otherBoard.blackRooks) {
            return false;
        }
        if (this.whiteQueens != otherBoard.whiteQueens) {
            return false;
        }
        if (this.blackQueens != otherBoard.blackQueens) {
            return false;
        }
        if (this.whitePieces != otherBoard.whitePieces) {
            return false;
        }
        if (this.blackPieces != otherBoard.blackPieces) {
            return false;
        }
        if (strict) {
            if (this.moveCounter != otherBoard.moveCounter) {
                return false;
            }
            return this.fiftyCounter == otherBoard.fiftyCounter;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        }
        Board that = (Board) obj;
        if (!equalExceptMoveHistory(that, true)) {
            return false;
        }
        return this.undoStack.equals(that.undoStack);
    }

    private Square findKingSquare(King kingSquare) {
        Square ksq = null;
        List<Square> squares = Square.allSquares();
        for (Square sq : squares) {
            Piece p = getPiece(sq);
            if (kingSquare.equals(p)) {
                ksq = sq;
                break;
            }
        }
        assert (ksq != null);
        return ksq;
    }

    public void flipVertical() {

        List<Square> squares = Square.allSquares();
        Map<Square, Piece> myPieceMap = new HashMap<Square, Piece>();
        // remove pieces, remembering where they were
        for (Square sq : squares) {
            Piece p = clearSquare(sq);
            myPieceMap.put(sq, p);
        }

        // now flip them around
        for (Square sq : squares) {
            Piece p = myPieceMap.get(sq);
            if (p != null) {
                addPiece(p.getOppositeColorPiece(), sq.flipVertical());
            }
        }

        swapPlayer();

        Square myEP = clearEPSquare();
        if (myEP != null) {
            setEP(myEP.flipVertical());
        }

        // flip castling rights
        MyCastlingRights myCastlingRights = new MyCastlingRights(castlingRights.getValue());
        castlingRights.clear();
        if (myCastlingRights.isWhiteKingside()) {
            addCastlingRight(CastlingRights.BLACK_KINGSIDE);
        }
        if (myCastlingRights.isWhiteQueenside()) {
            addCastlingRight(CastlingRights.BLACK_QUEENSIDE);
        }
        if (myCastlingRights.isBlackKingside()) {
            addCastlingRight(CastlingRights.WHITE_KINGSIDE);
        }
        if (myCastlingRights.isBlackQueenside()) {
            addCastlingRight(CastlingRights.WHITE_QUEENSIDE);
        }

        resetKingSquares();
    }

    private Square getBlackKingSquare() {
        assert (blackKingSquare != null);
        assert (blackKingSquare.equals(findKingSquare(King.BLACK_KING)));
        return blackKingSquare;
    }

    public Square getEPSquare() {
        return epSquare;
    }

    public int getFiftyCounter() {
        return fiftyCounter;
    }

    public void setFiftyCounter(int fiftyCounter) {
        this.fiftyCounter = fiftyCounter;
    }

    public Square getKingSquare(Color player) {
        return player.isWhite() ? getWhiteKingSquare() : getBlackKingSquare();
    }

    public int getMoveCounter() {
        return moveCounter;
    }

    public void setMoveCounter(int moveCounter) {
        this.moveCounter = moveCounter;
    }

    public int getNumPieces(Piece p) {
        return pieceCountsMap.get(p);
    }

    public long getPawnKey() {
        assert (pawnKey == Zobrist.getPawnKey(this));
        return pawnKey;
    }

    public Piece getPiece(Square square) {
        return pieceMap.get(square);
    }

    public Color getPlayerToMove() {
        return playerToMove;
    }

    public List<Undo> getUndos() {
        return Collections.unmodifiableList(undoStack);
    }

    private Square getWhiteKingSquare() {
        assert (whiteKingSquare != null);
        assert (whiteKingSquare.equals(findKingSquare(King.WHITE_KING)));
        return whiteKingSquare;
    }

    public long getZobristKey() {
        assert (zobristKey == Zobrist.getBoardKey(this));
        return zobristKey;
    }

    public boolean hasCastlingRight(CastlingRights cr) {
        if (CastlingRights.WHITE_KINGSIDE == cr) {
            return castlingRights.isWhiteKingside();
        } else if (CastlingRights.WHITE_QUEENSIDE == cr) {
            return castlingRights.isWhiteQueenside();
        } else if (CastlingRights.BLACK_KINGSIDE == cr) {
            return castlingRights.isBlackKingside();
        } else if (CastlingRights.BLACK_QUEENSIDE == cr) {
            return castlingRights.isBlackQueenside();
        } else {
            throw new IllegalArgumentException("unknown castling right: " + cr);
        }
    }

    @Override
    public int hashCode() {
        int hash = hashCodeWithoutMoveHistory(true);
        hash = hash * 31 + undoStack.hashCode();

        return hash;
    }

    public int hashCodeWithoutMoveHistory(boolean strict) {
        int hash = pieceMap.hashCode();
        hash = hash * 17 + castlingRights.hashCode();
        hash = hash * 13 + playerToMove.hashCode();
        hash = hash * 31 + (epSquare == null ? 0 : epSquare.hashCode());
        hash = hash * 17 + (whiteKingSquare == null ? 0 : whiteKingSquare.hashCode());
        hash = hash * 13 + (blackKingSquare == null ? 0 : blackKingSquare.hashCode());
        hash = hash * 23 + Long.valueOf(whitePawns).hashCode();
        hash = hash * 29 + Long.valueOf(blackPawns).hashCode();
        hash = hash * 31 + Long.valueOf(whiteKnights).hashCode();
        hash = hash * 37 + Long.valueOf(blackKnights).hashCode();
        hash = hash * 43 + Long.valueOf(whiteBishops).hashCode();
        hash = hash * 47 + Long.valueOf(blackBishops).hashCode();
        hash = hash * 53 + Long.valueOf(whiteRooks).hashCode();
        hash = hash * 59 + Long.valueOf(blackRooks).hashCode();
        hash = hash * 61 + Long.valueOf(whiteQueens).hashCode();
        hash = hash * 67 + Long.valueOf(blackQueens).hashCode();
        hash = hash * 71 + Long.valueOf(whitePieces).hashCode();
        hash = hash * 73 + Long.valueOf(blackPieces).hashCode();

        if (strict) {
            hash = hash * 17 + moveCounter;
            hash = hash * 13 + fiftyCounter;
        }
        return hash;
    }

    public boolean isEmpty(Square square) {
        return pieceMap.get(square) == null;
    }

    public boolean isOpponentInCheck() {
        return AttackDetector.attacked(this, getKingSquare(Color.swap(playerToMove)), playerToMove);
    }

    public boolean isPlayerInCheck() {
        return AttackDetector.attacked(this, getKingSquare(playerToMove), Color.swap(playerToMove));
    }

    public void movePiece(Square from, Square to) {
        if (!from.equals(to)) {
            addPiece(getPiece(from), to);
            clearSquare(from);
        }
    }

    public void resetBoard() {
        undoStack.clear();
        pieceMap.clear();
        pieceCountsMap.put(Queen.WHITE_QUEEN, 0);
        pieceCountsMap.put(Queen.BLACK_QUEEN, 0);
        pieceCountsMap.put(Rook.WHITE_ROOK, 0);
        pieceCountsMap.put(Rook.BLACK_ROOK, 0);
        pieceCountsMap.put(Knight.WHITE_KNIGHT, 0);
        pieceCountsMap.put(Knight.BLACK_KNIGHT, 0);
        pieceCountsMap.put(Bishop.WHITE_BISHOP, 0);
        pieceCountsMap.put(Bishop.BLACK_BISHOP, 0);
        pieceCountsMap.put(King.WHITE_KING, 0);
        pieceCountsMap.put(King.BLACK_KING, 0);
        pieceCountsMap.put(Pawn.WHITE_PAWN, 0);
        pieceCountsMap.put(Pawn.BLACK_PAWN, 0);

        whitePawns = blackPawns = 0;
        whiteKnights = blackKnights = 0;
        whiteBishops = blackBishops = 0;
        whiteRooks = blackRooks = 0;
        whiteQueens = blackQueens = 0;
        whitePieces = blackPieces = 0;

        zobristKey = 0;
        pawnKey = 0;

        addPiece(Rook.BLACK_ROOK, Square.valueOf(File.FILE_A, Rank.RANK_8));
        addPiece(Knight.BLACK_KNIGHT, Square.valueOf(File.FILE_B, Rank.RANK_8));
        addPiece(Bishop.BLACK_BISHOP, Square.valueOf(File.FILE_C, Rank.RANK_8));
        addPiece(Queen.BLACK_QUEEN, Square.valueOf(File.FILE_D, Rank.RANK_8));
        addPiece(King.BLACK_KING, Square.valueOf(File.FILE_E, Rank.RANK_8));
        addPiece(Bishop.BLACK_BISHOP, Square.valueOf(File.FILE_F, Rank.RANK_8));
        addPiece(Knight.BLACK_KNIGHT, Square.valueOf(File.FILE_G, Rank.RANK_8));
        addPiece(Rook.BLACK_ROOK, Square.valueOf(File.FILE_H, Rank.RANK_8));
        List<Square> squares = Square.rankSquares(Rank.RANK_7);
        for (Square sq : squares) {
            addPiece(Pawn.BLACK_PAWN, sq);
        }
        squares = Square.rankSquares(Rank.RANK_2);
        for (Square sq : squares) {
            addPiece(Pawn.WHITE_PAWN, sq);
        }
        addPiece(Rook.WHITE_ROOK, Square.valueOf(File.FILE_A, Rank.RANK_1));
        addPiece(Knight.WHITE_KNIGHT, Square.valueOf(File.FILE_B, Rank.RANK_1));
        addPiece(Bishop.WHITE_BISHOP, Square.valueOf(File.FILE_C, Rank.RANK_1));
        addPiece(Queen.WHITE_QUEEN, Square.valueOf(File.FILE_D, Rank.RANK_1));
        addPiece(King.WHITE_KING, Square.valueOf(File.FILE_E, Rank.RANK_1));
        addPiece(Bishop.WHITE_BISHOP, Square.valueOf(File.FILE_F, Rank.RANK_1));
        addPiece(Knight.WHITE_KNIGHT, Square.valueOf(File.FILE_G, Rank.RANK_1));
        addPiece(Rook.WHITE_ROOK, Square.valueOf(File.FILE_H, Rank.RANK_1));

        castlingRights.clear();
        Set<CastlingRights> crs = EnumSet.allOf(CastlingRights.class);
        for (CastlingRights cr : crs) {
            addCastlingRight(cr);
        }

        playerToMove = Color.WHITE;
        zobristKey ^= Zobrist.getPlayerKey(Color.WHITE);

        epSquare = null;
        whiteKingSquare = Square.valueOf(File.FILE_E, Rank.RANK_1);
        blackKingSquare = Square.valueOf(File.FILE_E, Rank.RANK_8);
        moveCounter = 0;
        fiftyCounter = 0;
    }

    public void resetKingSquares() {
        blackKingSquare = findKingSquare(King.BLACK_KING);
        whiteKingSquare = findKingSquare(King.WHITE_KING);
    }

    public void setEP(Square ep) {
        assert (ep != null);
        epSquare = ep;
        zobristKey ^= Zobrist.getEnPassantKey(ep);
    }

    public void swapPlayer() {
        zobristKey ^= Zobrist.getPlayerKey(playerToMove);
        playerToMove = Color.swap(playerToMove);
        zobristKey ^= Zobrist.getPlayerKey(playerToMove);
    }

    private void undoKingMove(Undo u, Piece p) {
        if (p.isWhite()) {
            whiteKingSquare = u.getMove().from();
            if (u.getMove().from().equals(Square.valueOf(File.FILE_E, Rank.RANK_1))) {
                if (u.getMove().to().equals(Square.valueOf(File.FILE_G, Rank.RANK_1))) {
                    movePiece(Square.valueOf(File.FILE_F, Rank.RANK_1), Square.valueOf(File.FILE_H, Rank.RANK_1));
                } else if (u.getMove().to().equals(Square.valueOf(File.FILE_C, Rank.RANK_1))) {
                    movePiece(Square.valueOf(File.FILE_D, Rank.RANK_1), Square.valueOf(File.FILE_A, Rank.RANK_1));
                }
            }
        } else {
            blackKingSquare = u.getMove().from();
            if (u.getMove().from().equals(Square.valueOf(File.FILE_E, Rank.RANK_8))) {
                if (u.getMove().to().equals(Square.valueOf(File.FILE_G, Rank.RANK_8))) {
                    movePiece(Square.valueOf(File.FILE_F, Rank.RANK_8), Square.valueOf(File.FILE_H, Rank.RANK_8));
                } else if (u.getMove().to().equals(Square.valueOf(File.FILE_C, Rank.RANK_8))) {
                    movePiece(Square.valueOf(File.FILE_D, Rank.RANK_8), Square.valueOf(File.FILE_A, Rank.RANK_8));
                }
            }
        }
    }

    public void undoLastMove() {
        assert (verify());
        assert (undoStack.size() > 0);
        int ind = undoStack.size() - 1;
        Undo u = undoStack.remove(ind);
        swapPlayer();
        epSquare = u.getEpSquare();
        moveCounter--;
        fiftyCounter = u.getFiftyCounter();
        castlingRights.setValue(u.getCastlingRights());

        Piece p = clearSquare(u.getMove().to());
        if (u.getMove().promotion() != null) {
            undoPromotion(u);
        } else if (p instanceof Pawn) {
            undoPawnMove(u, p);
        } else {
            if (p instanceof King) {
                undoKingMove(u, p);
            }
            addPiece(u.getMove().captured(), u.getMove().to());
            addPiece(p, u.getMove().from());
        }

        zobristKey = u.getZobristKey();
        assert (verify());
    }

    private void undoPawnMove(Undo u, Piece p) {
        if (p.isWhite()) {
            if (u.getMove().to().equals(epSquare)) {
                addPiece(
                        u.getMove().captured(),
                        Square.valueOf(epSquare.file(), epSquare.rank().south()));
                addPiece(p, u.getMove().from());
            } else {
                addPiece(u.getMove().captured(), u.getMove().to());
                addPiece(p, u.getMove().from());
            }
        } else {
            if (u.getMove().to().equals(epSquare)) {
                addPiece(
                        u.getMove().captured(),
                        Square.valueOf(epSquare.file(), epSquare.rank().north()));
                addPiece(p, u.getMove().from());
            } else {
                addPiece(u.getMove().captured(), u.getMove().to());
                addPiece(p, u.getMove().from());
            }
        }
    }

    private void undoPromotion(Undo u) {
        addPiece(u.getMove().captured(), u.getMove().to());
        addPiece(
                playerToMove.equals(Color.WHITE) ? Pawn.WHITE_PAWN : Pawn.BLACK_PAWN,
                u.getMove().from());
    }

    private boolean whiteCanCastleKingSide() {
        if (!hasCastlingRight(CastlingRights.WHITE_KINGSIDE)) {
            return false;
        }

        boolean pathIsClear =
                isEmpty(Square.valueOf(File.FILE_F, Rank.RANK_1)) && isEmpty(Square.valueOf(File.FILE_G, Rank.RANK_1));
        if (!pathIsClear) {
            return false;
        }

        Color opponent = Color.swap(playerToMove);
        boolean wouldCrossCheck = AttackDetector.attacked(this, Square.valueOf(File.FILE_E, Rank.RANK_1), opponent)
                || AttackDetector.attacked(this, Square.valueOf(File.FILE_F, Rank.RANK_1), opponent);
        return !wouldCrossCheck;
    }

    private boolean whiteCanCastleQueenSide() {
        if (!hasCastlingRight(CastlingRights.WHITE_QUEENSIDE)) {
            return false;
        }

        boolean pathIsClear = isEmpty(Square.valueOf(File.FILE_D, Rank.RANK_1))
                && isEmpty(Square.valueOf(File.FILE_C, Rank.RANK_1))
                && isEmpty(Square.valueOf(File.FILE_B, Rank.RANK_1));
        if (!pathIsClear) {
            return false;
        }

        Color opponent = Color.swap(playerToMove);
        boolean wouldCrossCheck = AttackDetector.attacked(this, Square.valueOf(File.FILE_E, Rank.RANK_1), opponent)
                || AttackDetector.attacked(this, Square.valueOf(File.FILE_D, Rank.RANK_1), opponent);
        return !wouldCrossCheck;
    }

    private boolean verify() {

        // test king placement
        Square myWhiteKingSq = null;
        Square myBlackKingSq = null;
        for (Square sq : Square.allSquares()) {
            Piece p = getPiece(sq);
            if (p == King.WHITE_KING) {
                assert (myWhiteKingSq == null);
                myWhiteKingSq = sq;
            }
            if (p == King.BLACK_KING) {
                assert (myBlackKingSq == null);
                myBlackKingSq = sq;
            }
        }
        assert (myWhiteKingSq != null && myWhiteKingSq == whiteKingSquare);
        assert (myBlackKingSq != null && myBlackKingSq == blackKingSquare);

        // ep square
        if (epSquare != null) {
            if (playerToMove == Color.BLACK) {
                assert (epSquare.rank() == Rank.RANK_3);
                assert (getPiece(North.getInstance().next(epSquare)) == Pawn.WHITE_PAWN);
            } else {
                assert (epSquare.rank() == Rank.RANK_6);
                assert (getPiece(South.getInstance().next(epSquare)) == Pawn.BLACK_PAWN);
            }
        }

        long myWhitePawns = 0, myBlackPawns = 0;
        long myWhiteKnights = 0, myBlackKnights = 0;
        long myWhiteBishops = 0, myBlackBishops = 0;
        long myWhiteRooks = 0, myBlackRooks = 0;
        long myWhiteQueens = 0, myBlackQueens = 0;
        long myWhitePieces = 0, myBlackPieces = 0;

        int numWhitePawns = 0, numBlackPawns = 0;
        int numWhiteKnights = 0, numBlackKnights = 0;
        int numWhiteBishops = 0, numBlackBishops = 0;
        int numWhiteRooks = 0, numBlackRooks = 0;
        int numWhiteQueens = 0, numBlackQueens = 0;

        for (Square sq : Square.allSquares()) {
            Piece p = getPiece(sq);
            long bb = Bitboard.squares[sq.value()];
            if (p != null) {
                if (p.isWhite()) {
                    myWhitePieces |= bb;
                } else {
                    myBlackPieces |= bb;
                }
            }

            if (Pawn.WHITE_PAWN == p) {
                myWhitePawns |= bb;
                numWhitePawns++;
            } else if (Pawn.BLACK_PAWN == p) {
                myBlackPawns |= bb;
                numBlackPawns++;
            } else if (Knight.WHITE_KNIGHT == p) {
                myWhiteKnights |= bb;
                numWhiteKnights++;
            } else if (Knight.BLACK_KNIGHT == p) {
                myBlackKnights |= bb;
                numBlackKnights++;
            } else if (Bishop.WHITE_BISHOP == p) {
                myWhiteBishops |= bb;
                numWhiteBishops++;
            } else if (Bishop.BLACK_BISHOP == p) {
                myBlackBishops |= bb;
                numBlackBishops++;
            } else if (Rook.WHITE_ROOK == p) {
                myWhiteRooks |= bb;
                numWhiteRooks++;
            } else if (Rook.BLACK_ROOK == p) {
                myBlackRooks |= bb;
                numBlackRooks++;
            } else if (Queen.WHITE_QUEEN == p) {
                myWhiteQueens |= bb;
                numWhiteQueens++;
            } else if (Queen.BLACK_QUEEN == p) {
                myBlackQueens |= bb;
                numBlackQueens++;
            }
        }

        assert (whitePawns == myWhitePawns);
        assert (blackPawns == myBlackPawns);
        assert (whiteKnights == myWhiteKnights);
        assert (blackKnights == myBlackKnights);
        assert (whiteBishops == myWhiteBishops);
        assert (blackBishops == myBlackBishops);
        assert (whiteRooks == myWhiteRooks);
        assert (blackRooks == myBlackRooks);
        assert (whiteQueens == myWhiteQueens);
        assert (blackQueens == myBlackQueens);
        assert (whitePieces == myWhitePieces);
        assert (blackPieces == myBlackPieces);

        assert (pieceCountsMap.get(Pawn.WHITE_PAWN) == numWhitePawns);
        assert (pieceCountsMap.get(Pawn.BLACK_PAWN) == numBlackPawns);
        assert (pieceCountsMap.get(Knight.WHITE_KNIGHT) == numWhiteKnights);
        assert (pieceCountsMap.get(Knight.BLACK_KNIGHT) == numBlackKnights);
        assert (pieceCountsMap.get(Bishop.WHITE_BISHOP) == numWhiteBishops);
        assert (pieceCountsMap.get(Bishop.BLACK_BISHOP) == numBlackBishops);
        assert (pieceCountsMap.get(Rook.WHITE_ROOK) == numWhiteRooks);
        assert (pieceCountsMap.get(Rook.BLACK_ROOK) == numBlackRooks);
        assert (pieceCountsMap.get(Queen.WHITE_QUEEN) == numWhiteQueens);
        assert (pieceCountsMap.get(Queen.BLACK_QUEEN) == numBlackQueens);

        return true;
    }

    public long getWhitePawns() {
        return whitePawns;
    }

    public long getBlackPawns() {
        return blackPawns;
    }

    public long getWhiteKnights() {
        return whiteKnights;
    }

    public long getBlackKnights() {
        return blackKnights;
    }

    public long getWhiteBishops() {
        return whiteBishops;
    }

    public long getBlackBishops() {
        return blackBishops;
    }

    public long getWhiteRooks() {
        return whiteRooks;
    }

    public long getBlackRooks() {
        return blackRooks;
    }

    public long getWhiteQueens() {
        return whiteQueens;
    }

    public long getBlackQueens() {
        return blackQueens;
    }

    public long getWhitePieces() {
        return whitePieces;
    }

    public long getBlackPieces() {
        return blackPieces;
    }
}

class MyCastlingRights {

    private static final int WHITE_KINGSIDE = 0x01;
    private static final int WHITE_QUEENSIDE = 0x02;
    private static final int BLACK_KINGSIDE = 0x04;
    private static final int BLACK_QUEENSIDE = 0x08;

    private int castlingRights;

    public MyCastlingRights() {
        clear();
    }

    public MyCastlingRights(int castlingRights) {
        this.castlingRights = castlingRights;
    }

    public void setWhiteKingside() {
        castlingRights |= WHITE_KINGSIDE;
    }

    public void setWhiteQueenside() {
        castlingRights |= WHITE_QUEENSIDE;
    }

    public void setBlackKingside() {
        castlingRights |= BLACK_KINGSIDE;
    }

    public void setBlackQueenside() {
        castlingRights |= BLACK_QUEENSIDE;
    }

    public void removeWhiteKingside() {
        castlingRights &= ~WHITE_KINGSIDE;
    }

    public void removeWhiteQueenside() {
        castlingRights &= ~WHITE_QUEENSIDE;
    }

    public void removeBlackKingside() {
        castlingRights &= ~BLACK_KINGSIDE;
    }

    public void removeBlackQueenside() {
        castlingRights &= ~BLACK_QUEENSIDE;
    }

    public boolean isWhiteKingside() {
        return (castlingRights & WHITE_KINGSIDE) > 0;
    }

    public boolean isWhiteQueenside() {
        return (castlingRights & WHITE_QUEENSIDE) > 0;
    }

    public boolean isBlackKingside() {
        return (castlingRights & BLACK_KINGSIDE) > 0;
    }

    public boolean isBlackQueenside() {
        return (castlingRights & BLACK_QUEENSIDE) > 0;
    }

    public void addAll() {
        castlingRights = WHITE_KINGSIDE | WHITE_QUEENSIDE | BLACK_KINGSIDE | BLACK_QUEENSIDE;
    }

    public void clear() {
        castlingRights = 0;
    }

    public int getValue() {
        return castlingRights;
    }

    public void setValue(int castlingRights) {
        this.castlingRights = castlingRights;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MyCastlingRights)) {
            return false;
        }

        MyCastlingRights that = (MyCastlingRights) obj;
        return this.castlingRights == that.castlingRights;
    }

    @Override
    public int hashCode() {
        return this.castlingRights;
    }
}
