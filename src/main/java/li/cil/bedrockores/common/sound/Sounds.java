package li.cil.bedrockores.common.sound;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public enum Sounds {
    INSTANCE;

    public SoundEvent bedrockMiner;

    public void init() {
        bedrockMiner = new SoundEvent(new ResourceLocation(Constants.MOD_ID, Constants.SOUND_BEDROCK_MINER));
    }
}
