package li.cil.bedrockores.common.command;

import net.minecraft.command.CommandBase;

abstract class AbstractCommand extends CommandBase {
    static String[] getSubArgs(final String[] args) {
        if (args.length == 0) {
            return args;
        }
        final String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        return subArgs;
    }
}
