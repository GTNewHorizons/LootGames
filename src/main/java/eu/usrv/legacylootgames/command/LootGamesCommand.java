package eu.usrv.legacylootgames.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import ru.timeconqueror.lootgames.common.config.LGConfigs;
import ru.timeconqueror.timecore.api.util.CollectionUtils;

import java.util.List;

public class LootGamesCommand extends CommandBase {
    private static final String PREFIX = "lootgames.command";
    private static final String USAGE = ".usage";

    @Override
    public String getCommandName() {
        return "lootgames";
    }

    @Override
    public String getCommandUsage(ICommandSender pCommandSender) {
        return PREFIX + USAGE;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            throw new WrongUsageException(PREFIX + USAGE);
        }

        String subCommand = args[0];

        if (subCommand.equals("reloadconfigs")) {
            LGConfigs.load();
            notifyOperators(sender, this, SubCommand.RELOAD_CONFIGS.prefix("success"));
        } else if (subCommand.equals("help")) {
            for (String command : SubCommand.COMMANDS) {
                notifyOperators(sender, this, PREFIX + "." + command + USAGE, "/" + getCommandName() + " " + command);
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, SubCommand.COMMANDS);
        }

        return null;
    }

    public enum SubCommand {
        RELOAD_CONFIGS("reloadconfigs"),
        HELP("help");

        private final String command;

        private static final String[] COMMANDS = CollectionUtils.mapArray(values(), String[]::new, subCommand -> subCommand.command);

        SubCommand(String command) {
            this.command = command;
        }

        public String prefix(String key) {
            return PREFIX + "." + command + "." + key;
        }
    }
}

