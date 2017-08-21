package li.cil.bedrockores.common.command;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandBedrockOres extends AbstractCommand {
    private final Map<String, CommandBase> subCommands = new HashMap<>();

    public CommandBedrockOres() {
        addSubCommand(new SubCommandWrap());
        addSubCommand(new SubCommandUnwrap());
    }

    private void addSubCommand(final CommandBase command) {
        subCommands.put(command.getName(), command);
    }

    // --------------------------------------------------------------------- //
    // Command

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getName() {
        return Constants.MOD_ID;
    }

    @Override
    public String getUsage(final ICommandSender sender) {
        return Constants.COMMAND_USAGE;
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        final CommandBase subCommand = subCommands.get(args[0]);
        if (subCommand == null) {
            throw new WrongUsageException(getUsage(sender));
        }

        subCommand.execute(server, sender, getSubArgs(args));
    }

    @Override
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, @Nullable final BlockPos targetPos) {
        if (args.length > 1) {
            final CommandBase subCommand = subCommands.get(args[0]);
            if (subCommand == null) {
                return Collections.emptyList();
            }

            return subCommand.getTabCompletions(server, sender, getSubArgs(args), targetPos);
        }

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, subCommands.keySet());
        }

        return Collections.emptyList();
    }
}
