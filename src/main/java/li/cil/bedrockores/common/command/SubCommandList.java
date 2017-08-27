package li.cil.bedrockores.common.command;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.OreConfig;
import li.cil.bedrockores.common.config.VeinConfig;
import li.cil.bedrockores.common.config.WrappedBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

final class SubCommandList extends AbstractSubCommand {
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) {
        notifyCommandListener(sender, this, Constants.COMMAND_LIST);
        for (final OreConfig ore : VeinConfig.INSTANCE.getOres()) {
            notifyCommandListener(sender, this, Constants.COMMAND_LIST_ITEM, ore.state.getBlockState().toString());
        }

        if (WrappedBlockState.ERRORED.isEmpty()) {
            return;
        }

        notifyCommandListener(sender, this, Constants.COMMAND_LIST_ERRORS);
        for (final WrappedBlockState state : WrappedBlockState.ERRORED) {
            notifyCommandListener(sender, this, Constants.COMMAND_LIST_ERROR_ITEM, state.toString());
        }
        notifyCommandListener(sender, this, Constants.COMMAND_LIST_ERRORS_EPILOGUE);
    }
}
