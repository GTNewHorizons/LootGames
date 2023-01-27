package com.jamesswafford.chess4j.hash;

import eu.usrv.yamcore.auxiliary.LogHelper;

public class PawnTranspositionTable {
    private static final PawnTranspositionTable INSTANCE = new PawnTranspositionTable();
    private static LogHelper mLog = new LogHelper("LootGames - ChessEngine");
    private static int numEntries;
    private static int mask = 0xFFFFF;

    static {
        numEntries = mask + 1;
        // numEntries should be a power of two
        assert ((numEntries & mask) == 0);
    }

    private PawnTranspositionTableEntry[] table;
    private long numProbes = 0;
    private long numHits = 0;
    private long numCollisions = 0;

    private PawnTranspositionTable() {
        table = new PawnTranspositionTableEntry[numEntries];
        mLog.debug("# pawn transposition table initialized with  " + numEntries + " entries.");
    }

    public static PawnTranspositionTable getInstance() {
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

    public PawnTranspositionTableEntry probe(long zobristKey) {
        numProbes++;
        PawnTranspositionTableEntry te = table[getMaskedKey(zobristKey)];

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

    public void store(long zobristKey, int score) {
        PawnTranspositionTableEntry te = new PawnTranspositionTableEntry(zobristKey, score);
        table[getMaskedKey(zobristKey)] = te;
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
