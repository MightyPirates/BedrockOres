package li.cil.bedrockores.common.world;

import com.mojang.serialization.Codec;
import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class BedrockOreFeature extends Feature<BedrockOreConfiguration> {
    public BedrockOreFeature(final Codec<BedrockOreConfiguration> config) {
        super(config);
    }

    public boolean place(FeaturePlaceContext<BedrockOreConfiguration> context) {
        final var config = context.config();
        final var origin = context.origin();
        final var level = context.level();
        final var random = context.random();

        final var radius = config.radius().sample(random);
        final var height = config.height().sample(random);

        assert radius > 0;
        assert height > 0;

        final var y = origin.getY();
        final var yShift = height / 2;
        final var yMaxInclusive = y + height - 1 - yShift;
        final var yMinInclusive = y - yShift;

        var didPlace = false;
        var tempPos = new BlockPos.MutableBlockPos();
        for (final BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, 0, -radius), origin.offset(radius, 0, radius))) {
            int dx = pos.getX() - origin.getX();
            int dz = pos.getZ() - origin.getZ();
            if (dx * dx + dz * dz <= radius * radius) {
                didPlace |= this.placeColumn(config, level, random, tempPos.set(pos), yMinInclusive, yMaxInclusive);
            }
        }
        return didPlace;
    }

    protected boolean placeColumn(BedrockOreConfiguration config, WorldGenLevel level, RandomSource random, BlockPos.MutableBlockPos pos, int yMinInclusive, int yMaxInclusive) {
        var didPlace = false;
        for (int y = yMaxInclusive; y >= yMinInclusive; --y) {
            pos.setY(y);

            if (!config.rule().test(level.getBlockState(pos), random)) {
                continue;
            }
            if (random.nextFloat() >= config.density()) {
                continue;
            }

            setBlock(level, pos, Blocks.BEDROCK_ORE.get().defaultBlockState());
            if (level.getBlockEntity(pos) instanceof BedrockOreBlockEntity bedrockOre) {
                bedrockOre.setOreBlockState(config.ore());
                config.amount()
                        .ifLeft(amount -> bedrockOre.setAmount(amount.sample(random)))
                        .ifRight(infinite -> bedrockOre.setInfinite());
            }
            didPlace = true;
        }
        return didPlace;
    }
}
