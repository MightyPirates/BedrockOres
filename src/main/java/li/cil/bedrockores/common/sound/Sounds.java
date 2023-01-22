package li.cil.bedrockores.common.sound;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class Sounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Constants.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<SoundEvent> MINER = SOUND_EVENTS.register("bedrock_miner", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Constants.MOD_ID, "bedrock_miner")));

    // --------------------------------------------------------------------- //

    public static void initialize() {
        SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
