package li.cil.bedrockores.common.command;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.OreConfig;
import li.cil.bedrockores.common.config.VeinConfig;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

final class SubCommandList extends AbstractSubCommand {
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        notifyCommandListener(sender, this, Constants.COMMAND_LIST);
        for (final OreConfig ore : VeinConfig.getOres()) {
            notifyCommandListener(sender, this, Constants.COMMAND_LIST_ITEM, ore.state.getBlockState().toString());
        }
    }
}
