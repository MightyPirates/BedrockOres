package li.cil.bedrockores.util;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

public final class WorldUtils {
    @Nullable
    public static TileEntity getTileEntityThreadsafe(final IBlockAccess world, final BlockPos pos) {
        return world instanceof ChunkCache ? ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
    }

    public static void playBreakSound(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos).getActualState(world, pos);
        final SoundType soundtype = state.getBlock().getSoundType(state, world, pos, null);
        world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, soundtype.getBreakSound(), SoundCategory.BLOCKS, soundtype.getVolume(), soundtype.getPitch());
    }

    // --------------------------------------------------------------------- //

    private WorldUtils() {
    }
}
