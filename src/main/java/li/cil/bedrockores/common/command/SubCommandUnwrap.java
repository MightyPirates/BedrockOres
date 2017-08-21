package li.cil.bedrockores.common.command;

import li.cil.bedrockores.common.tileentity.TileEntityBedrockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class SubCommandUnwrap extends AbstractSubCommand {
    @Override
    public String getName() {
        return "unwrap";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        final World world = sender.getEntityWorld();
        final BlockPos pos = getLookedAtBlockPos(sender);
        if (pos == null) {
            throw new WrongUsageException(getUsage(sender));
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityBedrockOre)) {
            return;
        }

        final IBlockState oreBlockState = ((TileEntityBedrockOre) tileEntity).getOreBlockState();
        if (oreBlockState != null) {
            world.setBlockState(pos, oreBlockState);
        } else {
            world.setBlockToAir(pos);
        }
    }
}
