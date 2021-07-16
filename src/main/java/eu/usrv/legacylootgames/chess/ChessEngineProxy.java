package eu.usrv.legacylootgames.chess;


import com.jamesswafford.chess4j.exceptions.IllegalMoveException;
import com.jamesswafford.chess4j.exceptions.ParseException;
import com.jamesswafford.chess4j.io.InputParser;
import ru.timeconqueror.lootgames.LootGames;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


interface IChessEventListener {
    void chessEngineMessage(UUID pUUID, String pMessage);
}

public class ChessEngineProxy {
    private static ChessEngineProxy _mInstance;
    private UUID _mCurrentAttachedChessGame = null;
    private final List<IChessEventListener> listeners = new ArrayList<IChessEventListener>();

    private ChessEngineProxy() {
    }

    public static ChessEngineProxy getInstance() {
        if (_mInstance == null)
            _mInstance = new ChessEngineProxy();

        return _mInstance;
    }

    public UUID getEngineToken() {
        if (_mCurrentAttachedChessGame != null)
            return null;
        else
            _mCurrentAttachedChessGame = UUID.randomUUID();

        return _mCurrentAttachedChessGame;
    }

    public void resetEngine() {
        sendCommand("force");
        sendCommand("new");
        sendCommand("level 10 99 0");
        sendCommand("easy");
    }

    private boolean sendCommand(String pCommand) {
        try {
            InputParser.getInstance().parseCommand(pCommand);
        } catch (IllegalMoveException ime) {
            LootGames.LOGGER.error("Illegal move");
        } catch (ParseException pe) {
            LootGames.LOGGER.error("Parse error: " + pe.getMessage());
        } catch (Exception e) {
            LootGames.LOGGER.debug("Caught (hopefully recoverable) exception: " + e.getMessage());
        }
        return true;
    }

    public void addListener(IChessEventListener toAdd) {
        listeners.add(toAdd);
    }

    public void publishAnswer(String pMessage) {
        for (IChessEventListener hl : listeners)
            hl.chessEngineMessage(_mCurrentAttachedChessGame, pMessage);
    }
}