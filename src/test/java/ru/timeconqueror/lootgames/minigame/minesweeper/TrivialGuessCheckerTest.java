package ru.timeconqueror.lootgames.minigame.minesweeper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.timeconqueror.lootgames.api.util.Pos2i;

class TrivialGuessCheckerTest {

    MSBoard board;
    TrivialGuessChecker checker;

    @BeforeEach
    void setUp() {
        board = new MSBoard(0, 0);
        checker = new TrivialGuessChecker(board);
    }

    @Test
    void CheckBoardRead() {
        board.readFromTestString("12\n34");
        assertEquals(Type.ONE, board.getType(0, 0));
        assertEquals(Type.TWO, board.getType(1, 0));
        assertEquals(Type.THREE, board.getType(0, 1));
        assertEquals(Type.FOUR, board.getType(1, 1));
    }

    @Test
    void Pattern1and2() {
        String[] lines13 = { "X  X", "X   ", "    ", "   X" };
        String[] line2 = { "XX X", "X XX", " X X", "X  X", "XX  ", "  XX", "X X ", "XXXX", "X   ", " X  ", "  X ",
                "   X", "    " };
        for (int l1 = 0; l1 < lines13.length; l1++) {
            for (int l2 = 0; l2 < line2.length; l2++) {
                for (int l3 = 0; l3 < lines13.length; l3++) {
                    board.readFromTestString(String.join("\n", "XXXX", lines13[l1], line2[l2], lines13[l3]));
                    boolean isTrivial = l1 == 0 && l2 <= 1 && l3 == 0;
                    assertEquals(isTrivial, checker.isTrivial(new Pos2i(1, 2)) != null);
                    assertEquals(isTrivial, checker.isTrivial(new Pos2i(2, 2)) != null);
                }
            }

        }
    }

    @Test
    void Pattern3and4() {
        String[] lines14 = { "XXX", "XX ", "X X", "X  ", " XX", " X ", "  X", "   " };
        String[] lines23 = { " X ", "   " };
        for (int l1 = 0; l1 < lines14.length; l1++) {
            for (int l2 = 0; l2 < lines23.length; l2++) {
                for (int l3 = 0; l3 < lines23.length; l3++) {
                    for (int l4 = 0; l4 < lines14.length; l4++) {
                        board.readFromTestString(
                                String.join("X\n", lines14[l1], lines23[l2], lines23[l3], lines14[l4]) + "X");
                        boolean isTrivial = l1 == 0 && l2 != l3 && l4 == 0;
                        assertEquals(isTrivial, checker.isTrivial(new Pos2i(1, 1)) != null);
                        assertEquals(isTrivial, checker.isTrivial(new Pos2i(1, 2)) != null);

                    }
                }
            }

        }
    }

    @Test
    void Pattern5678() {
        String[] lines14 = { "X  X", "X   ", "   X", "    " };
        String[] lines23 = { " X  ", "  X " };
        for (int l1 = 0; l1 < lines14.length; l1++) {
            for (int l2 = 0; l2 < lines23.length; l2++) {
                for (int l3 = 0; l3 < lines23.length; l3++) {
                    for (int l4 = 0; l4 < lines14.length; l4++) {
                        String boardString = String
                                .join(" \n ", "     ", lines14[l1], lines23[l2], lines23[l3], lines14[l4], "     ");
                        board.readFromTestString(boardString);
                        boolean isTrivial = l1 == 0 && l2 != l3 && l4 == 0;
                        assertEquals(isTrivial, checker.isTrivial(new Pos2i(2, 2)) != null, "5\n" + boardString);
                        assertEquals(isTrivial, checker.isTrivial(new Pos2i(3, 3)) != null, "6\n" + boardString);
                        assertEquals(isTrivial, checker.isTrivial(new Pos2i(3, 2)) != null, "7\n" + boardString);
                        assertEquals(isTrivial, checker.isTrivial(new Pos2i(2, 3)) != null, "8\n" + boardString);

                    }
                }
            }
        }
    }

    @Test
    void PatternEdgeT() {
        String[] lines = { "   ", "  X", " X ", " XX", "X  ", "X X", "XX ", "XXX" };
        for (String l1 : lines) {
            for (String l2 : lines) {
                for (String l3 : lines) {
                    String boardString = String.join("\n  ", "     ", l1, l2, l3, "   ");
                    board.readFromTestString(boardString);

                    boolean isTrivial = l1.charAt(0) == 'X' && l2.charAt(0) == 'X'
                            && l3.charAt(0) == 'X'
                            && (l2.charAt(1) != l2.charAt(2));
                    boolean actual1 = checker.isTrivial(new Pos2i(3, 2)) != null;
                    boolean actual2 = checker.isTrivial(new Pos2i(4, 2)) != null;

                    assertEquals(isTrivial, actual1, "1\n" + boardString);
                    assertEquals(isTrivial, actual2, "2\n" + boardString);

                    boardString = String.join("  \n", "   ", l1, l2, l3, "     ");
                    board.readFromTestString(boardString);

                    isTrivial = l1.charAt(2) == 'X' && l2.charAt(2) == 'X'
                            && l3.charAt(2) == 'X'
                            && (l2.charAt(1) != l2.charAt(0));
                    actual1 = checker.isTrivial(new Pos2i(0, 2)) != null;
                    actual2 = checker.isTrivial(new Pos2i(1, 2)) != null;
                    assertEquals(isTrivial, actual1, "1\n" + boardString);
                    assertEquals(isTrivial, actual2, "2\n" + boardString);

                    boardString = String.join(" \n ", " " + l1, l2, l3, "   ", "    ");
                    board.readFromTestString(boardString);
                    isTrivial = l3.equals("XXX") && l1.charAt(1) != l2.charAt(1);
                    boolean actual3 = checker.isTrivial(new Pos2i(2, 1)) != null;
                    boolean actual4 = checker.isTrivial(new Pos2i(2, 0)) != null;

                    assertEquals(isTrivial, actual3, "3\n" + boardString);
                    assertEquals(isTrivial, actual4, "4\n" + boardString);

                    boardString = String.join(" \n ", "    ", "   ", l1, l2, l3 + " ");
                    board.readFromTestString(boardString);
                    isTrivial = l1.equals("XXX") && l2.charAt(1) != l3.charAt(1);
                    actual3 = checker.isTrivial(new Pos2i(2, 4)) != null;
                    actual4 = checker.isTrivial(new Pos2i(2, 3)) != null;

                    assertEquals(isTrivial, actual3, "3\n" + boardString);
                    assertEquals(isTrivial, actual4, "4\n" + boardString);
                }
            }
        }

    }

    @Test
    void PatternEdgeC() {
        String[] lines14 = { "  XX", "  X ", "   X", "    " };
        String[] lines23 = { "   X", "    " };
        for (String l1 : lines14) {
            for (String l2 : lines23) {
                for (String l3 : lines23) {
                    for (String l4 : lines14) {
                        String boardString = String.join("\n", l1, l2, l3, l4);
                        board.readFromTestString(boardString);
                        boolean isTrivial = l1.equals("  XX") && !l2.equals(l3) && l4.equals("  XX");
                        boolean actual3 = checker.isTrivial(new Pos2i(3, 2)) != null;
                        boolean actual4 = checker.isTrivial(new Pos2i(3, 1)) != null;

                        assertEquals(isTrivial, actual4, "4\n" + boardString);
                        assertEquals(isTrivial, actual3, "3\n" + boardString);

                        boardString = new StringBuilder(boardString).reverse().toString();
                        board.readFromTestString(boardString);

                        actual3 = checker.isTrivial(new Pos2i(0, 2)) != null;
                        actual4 = checker.isTrivial(new Pos2i(0, 1)) != null;
                        assertEquals(isTrivial, actual4, "4\n" + boardString);
                        assertEquals(isTrivial, actual3, "3\n" + boardString);

                    }
                }

            }
        }
        String[] lines1 = { "XXXX", "XXX ", "XX X", "XX  ", "X XX", "X X ", "X  X", "X   ", " XXX", " XX ", " X X",
                " X  ", "  XX", "X X ", "X  X", "X   " };
        String[] lines2 = { "X  X", "X   ", "   X", "    " };
        for (String l1 : lines1) {
            for (String l2 : lines2) {

                boolean isTrivial = (l1.equals("XX X") || l1.equals("X XX")) && l2.charAt(0) == 'X'
                        && l2.charAt(3) == 'X';
                String boardString = String.join("\n", l1, l2, "    ", "    ");
                board.readFromTestString(boardString);

                boolean actual1 = checker.isTrivial(new Pos2i(1, 0)) != null;
                boolean actual2 = checker.isTrivial(new Pos2i(2, 0)) != null;
                assertEquals(isTrivial, actual1, "1\n" + boardString);
                assertEquals(isTrivial, actual2, "2\n" + boardString);

                boardString = new StringBuilder(boardString).reverse().toString();
                board.readFromTestString(boardString);

                actual1 = checker.isTrivial(new Pos2i(1, 3)) != null;
                actual2 = checker.isTrivial(new Pos2i(2, 3)) != null;

                assertEquals(isTrivial, actual1, "1\n" + boardString);
                assertEquals(isTrivial, actual2, "2\n" + boardString);

            }
        }
    }

    @Test
    void Pattern5678All() {
        String boardPattern1 = String.join(
                "\n",
                "X  X  X ",
                " X  X  X",
                "  X  X  ",
                "X   X X ",
                " X X   X",
                "  X  X  ",
                "X  X  X ",
                " X  X  X");
        String boardPattern2 = String.join(
                "\n",
                " X  X  X",
                "X  X  X ",
                "  X  X  ",
                " X X   X",
                "X   X X ",
                "  X  X  ",
                " X  X  X",
                "X  X  X ");

        board.readFromTestString(boardPattern1);
        for (int x = 0; x < board.size(); x++) {
            for (int y = 0; y < board.size(); y++) {
                boolean isTrivial = x % 3 != 2 && y % 3 != 2;
                boolean actual = checker.isTrivial(new Pos2i(x, y)) != null;
                assertEquals(isTrivial, actual, new Pos2i(x, y).toString());
            }
        }

        board.readFromTestString(boardPattern2);
        for (int x = 0; x < board.size(); x++) {
            for (int y = 0; y < board.size(); y++) {
                boolean isTrivial = x % 3 != 2 && y % 3 != 2;
                boolean actual = checker.isTrivial(new Pos2i(x, y)) != null;
                assertEquals(isTrivial, actual, new Pos2i(x, y).toString());
            }
        }

    }
}
