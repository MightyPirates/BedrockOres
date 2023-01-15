package li.cil.bedrockores.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record BedrockOreConfiguration(BlockState ore,
                                      IntProvider amount,
                                      IntProvider radius,
                                      IntProvider halfHeight,
                                      float density
) implements FeatureConfiguration {
    public static final Codec<BedrockOreConfiguration> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            BlockState.CODEC.fieldOf("ore").forGetter(BedrockOreConfiguration::ore),
            IntProvider.POSITIVE_CODEC.fieldOf("amount").forGetter(BedrockOreConfiguration::amount),
            IntProvider.codec(0, 8).optionalFieldOf("radius", ConstantInt.of(4)).forGetter(BedrockOreConfiguration::radius),
            IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("half_height", ConstantInt.of(2)).forGetter(BedrockOreConfiguration::halfHeight),
            Codec.floatRange(0, 1).optionalFieldOf("density", 0.75f).forGetter(BedrockOreConfiguration::density)
    ).apply(builder, BedrockOreConfiguration::new));
}
