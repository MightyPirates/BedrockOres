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

        final var y = origin.getY();
        final var y1 = y + 4;
        final var y0 = y - 4;
        final var radius = config.radius().sample(random);

        var didPlace = false;
        var tempPos = new BlockPos.MutableBlockPos();
        for (final BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, 0, -radius), origin.offset(radius, 0, radius))) {
            int dx = pos.getX() - origin.getX();
            int dz = pos.getZ() - origin.getZ();
            if (dx * dx + dz * dz <= radius * radius) {
                didPlace |= this.placeColumn(config, level, random, tempPos.set(pos), y0, y1);
            }
        }
        return didPlace;
    }

    protected boolean placeColumn(BedrockOreConfiguration config, WorldGenLevel level, RandomSource random, BlockPos.MutableBlockPos center, int y0, int y1) {
        var didPlace = false;
        for (int i = y1; i > y0; --i) {
            center.setY(i);

            if (!level.getBlockState(center).is(net.minecraft.world.level.block.Blocks.BEDROCK)) {
                continue;
            }
            if (random.nextFloat() >= config.density()) {
                continue;
            }

            setBlock(level, center, Blocks.BEDROCK_ORE.get().defaultBlockState());
            if (level.getBlockEntity(center) instanceof BedrockOreBlockEntity bedrockOre) {
                bedrockOre.setOreBlockState(config.ore());
                bedrockOre.setAmount(config.amount().sample(random));
            }
            didPlace = true;
        }
        return didPlace;
    }
}
