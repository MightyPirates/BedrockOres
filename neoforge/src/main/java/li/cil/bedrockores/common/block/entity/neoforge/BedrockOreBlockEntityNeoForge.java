/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.block.entity.neoforge;

import li.cil.bedrockores.client.render.neoforge.Ore;
import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public final class BedrockOreBlockEntityNeoForge extends BedrockOreBlockEntity {
    public BedrockOreBlockEntityNeoForge(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    // --------------------------------------------------------------------- //

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull ModelData getModelData() {
        return Ore.create(this).asModelData();
    }

    @Override
    protected void onRenderDataChanged() {
        requestModelDataUpdate();
    }
}
