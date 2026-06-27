package ru.timeconqueror.lootgames.minigame.minesweeper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.timeconqueror.lootgames.api.util.Pos2i;

public class MSBoardSolverTest {

    MSBoard board;
    MSBoardSolver solver;
    private final MSBoardSolver.SolverLogic SOLVED = MSBoardSolver.SolverLogic.SOLVED;
    private final MSBoardSolver.SolverLogic LOCAL_LOGIC = MSBoardSolver.SolverLogic.LOCAL_LOGIC;
    private final MSBoardSolver.SolverLogic GLOBAL_LOGIC = MSBoardSolver.SolverLogic.GLOBAL_LOGIC;
    private final MSBoardSolver.SolverLogic NO_LOGIC_LEFT = MSBoardSolver.SolverLogic.NO_LOGIC_LEFT;

    @BeforeEach
    void setUp() {
        board = new MSBoard(0, 0);
    }

    private String padBoardData(String boardData) {
        int rowLength = boardData.indexOf('\n');
        int missingRows = (rowLength * rowLength + rowLength - 1 - boardData.length()) / rowLength;
        if (missingRows != 0) {
            StringBuilder b = new StringBuilder((rowLength + 1) * (rowLength + 1));
            for (int j = missingRows; j > 0; j--) {
                for (int k = rowLength; k > 0; k--) {
                    b.append(' ');
                }
                b.append('\n');
            }
            b.append(boardData);
            boardData = b.toString();
        }
        return boardData;
    }

    private void initSolver(String boardData) {

        board.readFromTestString(boardData);
        solver = new MSBoardSolver(board);
    }

    private void assertSolves(String boardData) {
        assertSolves(boardData, new Pos2i(0, 0));
    }

    private void assertSolves(String boardData, Pos2i startPos) {
        boardData = padBoardData(boardData);
        initSolver(boardData);
        MSBoardSolver.SolverLogic logic = solver.solve(startPos);
        assertEquals(SOLVED, logic);
        assertEquals("\n" + boardData, solver.boardKnowledgeToString());
    }

    private void assertStuck(String boardData) {
        assertStuck(boardData, new Pos2i(0, 0));
    }

    private void assertStuck(String boardData, Pos2i startPos) {
        boardData = padBoardData(boardData);
        initSolver(boardData);
        MSBoardSolver.SolverLogic logic = solver.solve(startPos);
        assertEquals(NO_LOGIC_LEFT, logic, boardData + "\n" + solver.boardKnowledgeToString());
    }

    @Test
    void testEmpty() {
        assertSolves("   \n   \n   ");
    }

    @Test
    void testSimple() {
        assertSolves(" 1X\n 11\n   ");
    }

    @Test
    void testSetMunge() {
        assertSolves("   \n111\n1X1");
        assertSolves("   \n121\nX2X");
        assertSolves("    \n    \n1111\nX11X");
        assertSolves("1111111\nX11X11X\n1111111");

    }

    @Test
    void testBruteForce() {
        assertSolves("    \n1111\nX22X\n12X2");
        assertSolves("    \n1111\nX22X\n2X21");
    }

    @Test
    void testStuck() {
        assertStuck(" 11\n13X\nX3X");
        assertStuck(" 11\n13X\n1XX");
        assertStuck(" 1X\n133\n1XX");
        assertStuck(" 11\n13X\n1XX");
    }
}
