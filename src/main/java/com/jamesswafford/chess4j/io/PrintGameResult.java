package com.jamesswafford.chess4j.io;


import com.jamesswafford.chess4j.Color;
import com.jamesswafford.chess4j.board.Board;
import com.jamesswafford.chess4j.utils.GameStatus;
import eu.usrv.legacylootgames.chess.ChessEngineProxy;


public final class PrintGameResult {
    private PrintGameResult() {
    }

    public static void printResult(GameStatus gs) {
        Board b = Board.INSTANCE;
        if (GameStatus.CHECKMATED.equals(gs)) {
            if (b.getPlayerToMove().equals(Color.WHITE)) {
                ChessEngineProxy.getInstance().publishAnswer("RESULT 0-1 {Black mates}\n");
            } else {
                ChessEngineProxy.getInstance().publishAnswer("RESULT 1-0 {White mates}\n");
            }
        } else if (GameStatus.STALEMATED.equals(gs)) {
            ChessEngineProxy.getInstance().publishAnswer("RESULT 1/2-1/2 {Stalemate}\n");
        } else if (GameStatus.DRAW_MATERIAL.equals(gs)) {
            ChessEngineProxy.getInstance().publishAnswer("RESULT 1/2-1/2 {Draw by lack of mating material}\n");
        } else if (GameStatus.DRAW_BY_50.equals(gs)) {
            ChessEngineProxy.getInstance().publishAnswer("RESULT 1/2-1/2 {Draw by 50 move rule}\n");
        } else if (GameStatus.DRAW_REP.equals(gs)) {
            ChessEngineProxy.getInstance().publishAnswer("RESULT 1/2-1/2 {Draw by repetition}\n");
        }
    }
}
