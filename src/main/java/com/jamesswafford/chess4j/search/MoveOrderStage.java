package com.jamesswafford.chess4j.search;

public enum MoveOrderStage {
    INITIAL,
    PV,
    HASH_MOVE,
    SCORE_MOVES,
    WINNING_CAPTURES,
    KILLER1,
    KILLER2,
    NONCAPTURES,
    LOSING_CAPTURES
}
