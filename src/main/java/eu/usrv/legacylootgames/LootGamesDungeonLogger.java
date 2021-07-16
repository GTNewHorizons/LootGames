package eu.usrv.legacylootgames;


import ru.timeconqueror.lootgames.common.config.LGConfigs;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;


/**
 * Simple logger to an external file, to record the creation of Dungeons
 */
public class LootGamesDungeonLogger {
    private FileHandler _mFileTxt;
    private VerySimpleFormatter _mFormatterTxt;
    private Logger _mLootgameLogger;

    public void setup() throws IOException {
        _mLootgameLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        _mLootgameLogger.setLevel(Level.ALL);
        _mFileTxt = new FileHandler("logs/LootGames.log", true);

        _mFormatterTxt = new VerySimpleFormatter();
        _mFileTxt.setFormatter(_mFormatterTxt);
        _mLootgameLogger.addHandler(_mFileTxt);
        _mLootgameLogger.setUseParentHandlers(false);

        info("== LootGames logger initialized ==");
    }

    public void info(String pMessage) {
        _mLootgameLogger.log(Level.INFO, pMessage);
    }

    public void debug(String pMessage) {
        if (LGConfigs.GENERAL.worldGen.dungeonLogLevel.equalsIgnoreCase("debug") || LGConfigs.GENERAL.worldGen.dungeonLogLevel.equalsIgnoreCase("trace"))
            _mLootgameLogger.log(Level.FINE, pMessage);
    }

    public void trace(String pMessage) {
        if (LGConfigs.GENERAL.worldGen.dungeonLogLevel.equalsIgnoreCase("trace"))
            _mLootgameLogger.log(Level.FINEST, pMessage);
    }

    public void info(String pMessage, Object... pArgs) {
        info(String.format(pMessage, pArgs));
    }

    public void debug(String pMessage, Object... pArgs) {
        debug(String.format(pMessage, pArgs));
    }

    public void trace(String pMessage, Object... pArgs) {
        trace(String.format(pMessage, pArgs));
    }

    private static class VerySimpleFormatter extends Formatter {
        // Create a DateFormat to format the logger timestamp.
        private static final DateFormat df = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss.SSS");

        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder(1000);
            builder.append(df.format(new Date(record.getMillis()))).append(" - ");
            builder.append("[").append(record.getSourceClassName()).append("]");
            builder.append("[").append(record.getLevel()).append("] - ");
            builder.append(formatMessage(record));
            builder.append(System.lineSeparator());
            return builder.toString();
        }

        public String getHead(Handler h) {
            return super.getHead(h);
        }

        public String getTail(Handler h) {
            return super.getTail(h);
        }
    }
}
