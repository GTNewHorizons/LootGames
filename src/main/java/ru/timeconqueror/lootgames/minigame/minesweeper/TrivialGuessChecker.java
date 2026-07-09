package ru.timeconqueror.lootgames.minigame.minesweeper;

import javax.annotation.Nullable;

import ru.timeconqueror.lootgames.api.util.Pos2i;

public class TrivialGuessChecker {

    private final MSBoard board;

    TrivialGuessChecker(MSBoard board) {
        this.board = board;
    }

    @Nullable
    public Pos2i isTrivial(Pos2i p) {
        // While they are quick to check for
        // They are not trivial to code...
        /**
         * <!-- spotless:off -->
         * Patterns to check for 12 34 56 78
         * x  x x  x
         * xX_x x_Xx
         * x  x x  x
         *
         * xxx xxx
         *  _   X
         *  X   _
         * xxx xxx
         *
         * x  x x  x
         *  X_   x_
         *  _x   _X
         * x  x x  x
         *
         * x  x x  x
         *  _X   _x
         *  x_   X_
         * x  x x  x
         *
         * <!-- spotless:on -->
         */
        int dontCheck = 0;

        if (!noKnowledge(p.add(-1, -1))) {
            dontCheck |= 0b00011001;
        }
        if (!noKnowledge(p.add(1, -1))) {
            dontCheck |= 0b01001010;
        }
        if (!noKnowledge(p.add(-1, 1))) {
            dontCheck |= 0b10000101;
        }
        if (!noKnowledge(p.add(1, 1))) {
            dontCheck |= 0b00100110;
        }
        if (!noKnowledge(p.add(-2, -1))) {
            dontCheck |= 0b01000010;
        }
        if (!noKnowledge(p.add(-1, -2))) {
            dontCheck |= 0b10000100;
        }
        if (!noKnowledge(p.add(2, -1))) {
            dontCheck |= 0b00010001;
        }
        if (!noKnowledge(p.add(1, -2))) {
            dontCheck |= 0b00100100;
        }
        if (!noKnowledge(p.add(-2, 1))) {
            dontCheck |= 0b00100010;
        }
        if (!noKnowledge(p.add(-1, 2))) {
            dontCheck |= 0b00011000;
        }
        if (!noKnowledge(p.add(2, 1))) {
            dontCheck |= 0b10000001;
        }
        if (!noKnowledge(p.add(1, 2))) {
            dontCheck |= 0b01001000;
        }
        if ((dontCheck & 1) == 0) {
            // Check 1
            if (noKnowledge(p.add(-1, 0)) && noKnowledge(p.add(2, 0))) {
                // Check for 1 clear and one bomb between flag walls
                if (isBomb(p)) {
                    if (isClear(p.add(1, 0))) return p.add(1, 0);
                } else {
                    if (isBomb(p.add(1, 0))) return p;
                }
            }
        }
        if ((dontCheck & 2) == 0) {
            // Check 2
            if (noKnowledge(p.add(-2, 0)) && noKnowledge(p.add(1, 0))) {
                if (isBomb(p)) {
                    if (isClear(p.add(-1, 0))) return p.add(-1, 0);
                } else {
                    if (isBomb(p.add(-1, 0))) return p;
                }
            }
        }
        if ((dontCheck & 4) == 0) {
            // Check 3
            if (noKnowledge(p.add(0, -2)) && noKnowledge(p.add(0, 1))) {
                if (isBomb(p)) {
                    if (isClear(p.add(0, -1))) return p.add(0, -1);
                } else {
                    if (isBomb(p.add(0, -1))) return p;
                }
            }
        }
        if ((dontCheck & 8) == 0) {
            // Check 4
            if (noKnowledge(p.add(0, -1)) && noKnowledge(p.add(0, 2))) {
                if (isBomb(p)) {
                    if (isClear(p.add(0, 1))) return p.add(0, 1);
                } else {
                    if (isBomb(p.add(0, 1))) return p;
                }
            }
        }
        if ((dontCheck & 16) == 0) {
            // Check 5
            if (noKnowledge(p.add(2, 2))) {
                if (isBomb(p)) {
                    if (isClear(p.add(1, 0)) && isClear(p.add(0, 1)) && isBomb(p.add(1, 1))) return p.add(1, 0);
                } else {
                    if (isBomb(p.add(1, 0)) && isBomb(p.add(0, 1)) && isClear(p.add(1, 1))) return p;
                }
            }
        }
        if ((dontCheck & 32) == 0) {
            // Check 6
            if (noKnowledge(p.add(-2, -2))) {
                if (isBomb(p)) {
                    if (isClear(p.add(-1, 0)) && isClear(p.add(0, -1)) && isBomb(p.add(-1, -1))) return p.add(-1, 0);
                } else {
                    if (isBomb(p.add(-1, 0)) && isBomb(p.add(0, -1)) && isClear(p.add(-1, -1))) return p;
                }
            }
        }
        if ((dontCheck & 64) == 0) {
            // Check 7
            if (noKnowledge(p.add(-2, 2))) {
                if (isBomb(p)) {
                    if (isClear(p.add(-1, 0)) && isClear(p.add(0, 1)) && isBomb(p.add(-1, 1))) return p.add(-1, 0);
                } else {
                    if (isBomb(p.add(-1, 0)) && isBomb(p.add(0, 1)) && isClear(p.add(-1, 1))) return p;
                }
            }
        }
        if ((dontCheck & 128) == 0) {
            // Check 8
            if (noKnowledge(p.add(2, -2))) {
                if (isBomb(p)) {
                    if (isClear(p.add(1, 0)) && isClear(p.add(0, -1)) && isBomb(p.add(1, -1))) return p.add(1, 0);
                } else {
                    if (isBomb(p.add(1, 0)) && isBomb(p.add(0, -1)) && isClear(p.add(1, -1))) return p;
                }
            }
        }
        return null;
    }

    private boolean isBomb(Pos2i p) {
        return board.hasFieldOn(p) && board.isBomb(p);
    }

    private boolean isClear(Pos2i p) {
        return board.hasFieldOn(p) && !board.isBomb(p);
    }

    private boolean noKnowledge(Pos2i p) {
        return !board.hasFieldOn(p) || board.isBomb(p);
    }
}
