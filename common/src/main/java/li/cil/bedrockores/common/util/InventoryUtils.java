/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class InventoryUtils {
    @ExpectPlatform
    @Nullable
    public static ItemStack insert(final Level level, final BlockPos pos, final Direction side, final ItemStack stack) {
        throw new AssertionError();
    }

    private InventoryUtils() {
    }
}
