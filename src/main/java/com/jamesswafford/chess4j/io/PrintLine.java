package com.jamesswafford.chess4j.io;

import java.util.List;

import com.jamesswafford.chess4j.board.Move;
import eu.usrv.yamcore.auxiliary.LogHelper;

public class PrintLine {

    private static LogHelper mLog = new LogHelper("LootGames - ChessEngine");

    public static void printLine(List<Move> moves) {
        printLine(moves, false);
    }

    public static void printLine(List<Move> moves, boolean debug) {
        String ms = getMoveString(moves);
        if (debug) {
            mLog.debug(ms + "\n");
        } else {
            mLog.info(ms + "\n");
        }
    }

    public static void printLine(List<Move> moves, int depth, int score, long startTime, long nodes) {
        long timeInCentis = (System.currentTimeMillis() - startTime) / 10;
        String line = getMoveString(moves);
        String output = String.format("%2d %5d %5d %7d %s", depth, score, timeInCentis, nodes, line);
        mLog.info(output);
    }

    public static String getMoveString(List<Move> moves) {
        String s = "";
        for (Move m : moves) {
            s += m.toString() + " ";
        }
        return s;
    }
}
