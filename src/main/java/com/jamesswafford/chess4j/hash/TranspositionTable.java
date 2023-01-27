package com.jamesswafford.chess4j.hash;

import com.jamesswafford.chess4j.Constants;
import com.jamesswafford.chess4j.board.Move;
import eu.usrv.yamcore.auxiliary.LogHelper;

public class TranspositionTable {
    private static final TranspositionTable INSTANCE = new TranspositionTable();
    private static LogHelper mLog = new LogHelper("LootGames - ChessEngine");
    private static int numEntries;
    private static int mask = 0xFFFFF; // 1 million entries

    static {
        numEntries = mask + 1;
        // numEntries should be a power of two
        assert ((numEntries & mask) == 0);
    }

    private TranspositionTableEntry[] table;
    private long numProbes = 0;
    private long numHits = 0;
    private long numCollisions = 0;

    private TranspositionTable() {
        table = new TranspositionTableEntry[numEntries];
        clear();
        mLog.debug("# transposition table initialized with  " + numEntries + " entries.");
    }

    public static TranspositionTable getInstance() {
        return INSTANCE;
    }

    public void clear() {
        clearStats();
        for (int i = 0; i < numEntries; i++) {
            table[i] = null;
        }
    }

    public void clearStats() {
        numProbes = 0;
        numHits = 0;
        numCollisions = 0;
    }

    private int getMaskedKey(long zobristKey) {
        int iKey = ((int) zobristKey) & mask;
        return iKey;
    }

    public TranspositionTableEntry probe(long zobristKey) {
        numProbes++;
        TranspositionTableEntry te = table[getMaskedKey(zobristKey)];

        if (te != null) {
            // compare full signature to avoid collisions
            if (te.getZobristKey() != zobristKey) {
                numCollisions++;
                return null;
            } else {
                numHits++;
            }
        }

        return te;
    }

    /**
     * Store an entry in the transposition table, Gerbil style. Meaning, for now I'm skirting around
     * dealing with the headache that is storing mate scores by storing them as bounds only.
     *
     * @param entryType
     * @param zobristKey
     * @param score
     * @param depth
     * @param move
     */
    public void store(TranspositionTableEntryType entryType, long zobristKey, int score, int depth, Move move) {
        if (isMateScore(score)) {
            if (entryType == TranspositionTableEntryType.UPPER_BOUND) {
                // failing low on mate. don't allow a cutoff, just store any associated move
                entryType = TranspositionTableEntryType.MOVE_ONLY;
            } else {
                // convert to fail high
                entryType = TranspositionTableEntryType.LOWER_BOUND;
                score = getCheckMateBound();
            }
        } else if (isMatedScore(score)) {
            if (entryType == TranspositionTableEntryType.LOWER_BOUND) {
                // failing high on -mate.
                entryType = TranspositionTableEntryType.MOVE_ONLY;
            } else {
                // convert to fail low
                entryType = TranspositionTableEntryType.UPPER_BOUND;
                score = getCheckMatedBound();
            }
        }

        TranspositionTableEntry te = new TranspositionTableEntry(entryType, zobristKey, score, depth, move);
        table[getMaskedKey(zobristKey)] = te;
    }

    private boolean isMateScore(int score) {
        return score >= getCheckMateBound();
    }

    private boolean isMatedScore(int score) {
        return score <= getCheckMatedBound();
    }

    private int getCheckMateBound() {
        return Constants.CHECKMATE - 500;
    }

    private int getCheckMatedBound() {
        return -getCheckMateBound();
    }

    public long getNumProbes() {
        return numProbes;
    }

    public long getNumHits() {
        return numHits;
    }

    public long getNumCollisions() {
        return numCollisions;
    }
}
