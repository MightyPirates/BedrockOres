/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.block.entity.fabric;

import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public final class BedrockOreBlockEntityFabric extends BedrockOreBlockEntity implements RenderDataBlockEntity {
    public BedrockOreBlockEntityFabric(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    // --------------------------------------------------------------------- //

    @Nullable
    @Override
    public Object getRenderData() {
        final var oreBlockState = getOreBlockState();
        return oreBlockState.isAir() ? null : oreBlockState;
    }
}
