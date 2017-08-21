package li.cil.bedrockores.common.command;

import li.cil.bedrockores.common.init.Blocks;
import li.cil.bedrockores.common.tileentity.TileEntityBedrockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class SubCommandWrap extends AbstractSubCommand {
    @Override
    public String getName() {
        return "wrap";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        final int amount;
        if (args.length > 0) {
            amount = parseInt(args[0], 1);
        } else {
            amount = 100;
        }

        final World world = sender.getEntityWorld();
        final BlockPos pos = getLookedAtBlockPos(sender);
        if (pos == null) {
            throw new WrongUsageException(getUsage(sender));
        }
        final IBlockState state = world.getBlockState(pos);

        final IBlockState newBlockState = Blocks.bedrockOre.getDefaultState();
        if (newBlockState == state) {
            return;
        }

        world.setBlockState(pos, newBlockState, 3);
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityBedrockOre) {
            final TileEntityBedrockOre tileEntityBedrockOre = (TileEntityBedrockOre) tileEntity;
            tileEntityBedrockOre.setOreBlockState(state, amount);
        }
    }
}
