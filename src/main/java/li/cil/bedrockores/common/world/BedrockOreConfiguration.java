package li.cil.bedrockores.common.world;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public record BedrockOreConfiguration(BlockState ore,
                                      Either<IntProvider, String> amount,
                                      IntProvider radius,
                                      IntProvider height,
                                      float density,
                                      RuleTest rule
) implements FeatureConfiguration {
    public static final Codec<String> INFINITE_CODEC = Codec.STRING.flatXmap(BedrockOreConfiguration::verifyInfinite, BedrockOreConfiguration::verifyInfinite);
    public static final Codec<BedrockOreConfiguration> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            BlockState.CODEC.fieldOf("ore").forGetter(BedrockOreConfiguration::ore),
            Codec.either(IntProvider.POSITIVE_CODEC, INFINITE_CODEC).fieldOf("amount").forGetter(BedrockOreConfiguration::amount),
            IntProvider.codec(0, 8).optionalFieldOf("radius", ConstantInt.of(4)).forGetter(BedrockOreConfiguration::radius),
            IntProvider.POSITIVE_CODEC.optionalFieldOf("height", ConstantInt.of(8)).forGetter(BedrockOreConfiguration::height),
            Codec.floatRange(0, 1).optionalFieldOf("density", 0.75f).forGetter(BedrockOreConfiguration::density),
            RuleTest.CODEC.optionalFieldOf("rule", new BlockMatchTest(Blocks.BEDROCK)).forGetter(BedrockOreConfiguration::rule)
    ).apply(builder, BedrockOreConfiguration::new));

    private static DataResult<? extends String> verifyInfinite(final String value) {
        return "infinite".equals(value) ? DataResult.success(value) : DataResult.error(() -> "Expected 'infinite' or an integer value");
    }
}
