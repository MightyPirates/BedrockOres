package li.cil.bedrockores.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public record BedrockOreConfiguration(BlockState ore,
                                      IntProvider amount,
                                      IntProvider radius,
                                      IntProvider height,
                                      float density,
                                      RuleTest rule
) implements FeatureConfiguration {
    public static final Codec<BedrockOreConfiguration> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            BlockState.CODEC.fieldOf("ore").forGetter(BedrockOreConfiguration::ore),
            IntProvider.POSITIVE_CODEC.fieldOf("amount").forGetter(BedrockOreConfiguration::amount),
            IntProvider.codec(0, 8).optionalFieldOf("radius", ConstantInt.of(4)).forGetter(BedrockOreConfiguration::radius),
            IntProvider.POSITIVE_CODEC.optionalFieldOf("height", ConstantInt.of(8)).forGetter(BedrockOreConfiguration::height),
            Codec.floatRange(0, 1).optionalFieldOf("density", 0.75f).forGetter(BedrockOreConfiguration::density),
            RuleTest.CODEC.optionalFieldOf("rule", new BlockMatchTest(Blocks.BEDROCK)).forGetter(BedrockOreConfiguration::rule)
    ).apply(builder, BedrockOreConfiguration::new));
}
