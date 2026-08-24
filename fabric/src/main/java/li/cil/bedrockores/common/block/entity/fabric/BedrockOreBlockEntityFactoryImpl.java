/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.block.entity.fabric;

import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class BedrockOreBlockEntityFactoryImpl {
    public static BedrockOreBlockEntity create(final BlockPos pos, final BlockState state) {
        return new BedrockOreBlockEntityFabric(pos, state);
    }

    private BedrockOreBlockEntityFactoryImpl() {
    }
}
