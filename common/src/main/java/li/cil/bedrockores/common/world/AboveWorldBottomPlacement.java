/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public final class AboveWorldBottomPlacement extends PlacementModifier {
    public static final MapCodec<AboveWorldBottomPlacement> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        IntProvider.NON_NEGATIVE_CODEC.fieldOf("offset").forGetter(placement -> placement.offset)
    ).apply(builder, AboveWorldBottomPlacement::new));

    private final IntProvider offset;

    public AboveWorldBottomPlacement(final IntProvider offset) {
        this.offset = offset;
    }

    @Override
    public Stream<BlockPos> getPositions(final PlacementContext context, final RandomSource random, final BlockPos pos) {
        return Stream.of(pos.atY(context.getMinBuildHeight() + offset.sample(random)));
    }

    @Override
    public PlacementModifierType<?> type() {
        return BedrockOrePlacementModifiers.ABOVE_WORLD_BOTTOM.get();
    }
}
