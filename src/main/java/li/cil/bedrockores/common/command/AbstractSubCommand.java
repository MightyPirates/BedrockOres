package li.cil.bedrockores.common.command;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractSubCommand extends AbstractCommand {
    @Override
    public String getUsage(final ICommandSender sender) {
        return String.format(Constants.COMMAND_SUB_USAGE, getName());
    }

    @Nullable
    static BlockPos getLookedAtBlockPos(final ICommandSender sender) throws CommandException {
        final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        final World world = player.getEntityWorld();
        final Vec3d origin = player.getPositionEyes(1);
        final Vec3d lookVec = player.getLookVec();
        final float blockReachDistance = player.isCreative() ? 5 : 4.5f;
        final Vec3d lookAt = origin.add(lookVec.scale(blockReachDistance));
        final RayTraceResult hit = world.rayTraceBlocks(origin, lookAt);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }

        final BlockPos hitPos = hit.getBlockPos();
        if (!world.isBlockLoaded(hitPos)) {
            return null;
        }

        return hitPos;
    }
}
