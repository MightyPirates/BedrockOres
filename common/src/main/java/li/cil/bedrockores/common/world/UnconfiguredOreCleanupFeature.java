/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.world;

import com.mojang.serialization.Codec;
import li.cil.bedrockores.common.block.entity.UnconfiguredOreCleanup;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class UnconfiguredOreCleanupFeature extends Feature<NoneFeatureConfiguration> {
    public UnconfiguredOreCleanupFeature(final Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context) {
        final var level = context.level();
        return UnconfiguredOreCleanup.removeUnconfiguredFromChunk(level, level.getChunk(context.origin())) > 0;
    }
}
