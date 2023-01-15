package li.cil.bedrockores.common.world;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BedrockOreFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Constants.MOD_ID);

    public static final RegistryObject<BedrockOreFeature> BEDROCK_ORE = FEATURES.register("bedrock_ore", () -> new BedrockOreFeature(BedrockOreConfiguration.CODEC));

    public static void initialize() {
        FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
