package com.jamesswafford.chess4j.search;

import java.util.Arrays;
import java.util.List;

import com.jamesswafford.chess4j.board.Board;
import com.jamesswafford.chess4j.board.Move;
import com.jamesswafford.chess4j.utils.MoveUtils;

public class MoveOrderer {

    private Board board;
    private MoveOrderStage lastMoveOrderStage = MoveOrderStage.INITIAL;
    private Move[] moves;
    private Integer[] scores;
    private Move pvMove;
    private Move hashMove;
    private Move killer1, killer2;

    public MoveOrderer(Board board, List<Move> moves, Move pvMove, Move hashMove, Move killer1, Move killer2) {
        this.board = board;
        this.moves = moves.toArray(new Move[moves.size()]);
        this.scores = new Integer[moves.size()];
        this.pvMove = pvMove;
        this.hashMove = hashMove;
        this.killer1 = killer1;
        this.killer2 = killer2;
    }

    public Move selectNextMove(int startIndex) {

        assert (startIndex < moves.length);
        assert (lastMoveOrderStage != null);

        // try PV move
        if (lastMoveOrderStage == MoveOrderStage.INITIAL) {
            if (pvMove != null) {
                assert (MoveUtils.indexOf(moves, pvMove, 0) >= 0);
                MoveUtils.putMoveAtTop(moves, pvMove);
                lastMoveOrderStage = MoveOrderStage.PV;
                return pvMove;
            }
        }

        // try hash move
        if (lastMoveOrderStage.ordinal() <= MoveOrderStage.PV.ordinal()) {
            if (hashMove != null) {
                int ind = MoveUtils.indexOf(moves, hashMove, startIndex);
                if (ind != -1) {
                    assert (Arrays.asList(moves).subList(startIndex, moves.length).contains(hashMove));
                    MoveUtils.swap(moves, startIndex, ind);
                    lastMoveOrderStage = MoveOrderStage.HASH_MOVE;
                    return hashMove;
                }
            }
        }

        // score moves
        if (lastMoveOrderStage.ordinal() <= MoveOrderStage.SCORE_MOVES.ordinal()) {
            scoreMoves(startIndex);
        }

        // promotions and winning (non-losing) captures only
        if (lastMoveOrderStage.ordinal() <= MoveOrderStage.WINNING_CAPTURES.ordinal()) {
            int bestInd = getIndexOfBestCapture(startIndex);
            if (bestInd != -1 && scores[bestInd] >= 0) {
                MoveUtils.swap(moves, startIndex, bestInd);
                swapScores(startIndex, bestInd);
                lastMoveOrderStage = MoveOrderStage.WINNING_CAPTURES;
                return moves[startIndex];
            }
        }

        // killer1
        if (lastMoveOrderStage.ordinal() <= MoveOrderStage.KILLER1.ordinal() && killer1 != null) {
            int ind = MoveUtils.indexOf(moves, killer1, startIndex);
            if (ind != -1) {
                MoveUtils.swap(moves, startIndex, ind);
                swapScores(startIndex, ind);
                lastMoveOrderStage = MoveOrderStage.KILLER1;
                return moves[startIndex];
            }
        }

        // killer2
        if (lastMoveOrderStage.ordinal() <= MoveOrderStage.KILLER2.ordinal() && killer2 != null) {
            int ind = MoveUtils.indexOf(moves, killer2, startIndex);
            if (ind != -1) {
                MoveUtils.swap(moves, startIndex, ind);
                swapScores(startIndex, ind);
                lastMoveOrderStage = MoveOrderStage.KILLER2;
                return moves[startIndex];
            }
        }

        // non-captures
        if (lastMoveOrderStage.ordinal() <= MoveOrderStage.NONCAPTURES.ordinal()) {
            int ind = getIndexOfFirstNonCapture(startIndex);
            if (ind != -1) {
                MoveUtils.swap(moves, startIndex, ind);
                swapScores(startIndex, ind);
                lastMoveOrderStage = MoveOrderStage.NONCAPTURES;
                return moves[startIndex];
            }
        }

        // losing captures
        lastMoveOrderStage = MoveOrderStage.LOSING_CAPTURES;
        int loserInd = getIndexOfBestCapture(startIndex);
        if (loserInd != -1) {
            assert (scores[loserInd] < 0);
            MoveUtils.swap(moves, startIndex, loserInd);
            swapScores(startIndex, loserInd);
        }

        return moves[startIndex];
    }

    private void scoreMoves(int startIndex) {
        // up to startIndex has already been played, the score is irrelevant
        for (int i = startIndex; i < moves.length; i++) {
            scores[i] = MVVLVA.score(board, moves[i]);
        }
    }

    private int getIndexOfBestCapture(int startIndex) {
        int index = -1;
        int bestScore = -9999;

        for (int i = startIndex; i < moves.length; i++) {
            Move m = moves[i];
            if (m.captured() != null || m.promotion() != null) {
                int myScore = scores[i];
                if (myScore > bestScore) {
                    index = i;
                    bestScore = myScore;
                }
            }
        }
        return index;
    }

    private int getIndexOfFirstNonCapture(int startIndex) {
        int index = -1;

        for (int i = startIndex; i < moves.length; i++) {
            Move m = moves[i];
            if (m.captured() == null) {
                index = i;
                break;
            }
        }

        return index;
    }

    public void swapScores(int ind1, int ind2) {
        Integer tmp = scores[ind1];
        scores[ind1] = scores[ind2];
        scores[ind2] = tmp;
    }

    public MoveOrderStage getLastMoveOrderStage() {
        return lastMoveOrderStage;
    }

    public void setLastMoveOrderStage(MoveOrderStage lastMoveOrderStage) {
        this.lastMoveOrderStage = lastMoveOrderStage;
    }
}
