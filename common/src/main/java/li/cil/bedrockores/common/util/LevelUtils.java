/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public final class LevelUtils {
    @SuppressWarnings("deprecation")
    @Nullable
    public static BlockEntity getBlockEntityIfChunkLoaded(final BlockGetter level, final BlockPos pos) {
        if (level instanceof final LevelReader reader && !reader.hasChunkAt(pos)) {
            return null;
        }
        return level.getBlockEntity(pos);
    }

    private LevelUtils() {
    }
}
