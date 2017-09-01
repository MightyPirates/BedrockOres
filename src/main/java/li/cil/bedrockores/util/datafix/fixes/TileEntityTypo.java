package li.cil.bedrockores.util.datafix.fixes;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.Objects;

public final class TileEntityTypo implements IFixableData {
    private static final String TAG_ID = "id";
    private static final String TYPO_BEDROCK_ORE_ID = Constants.MOD_ID + ": " + Constants.NAME_BEDROCK_ORE;
    private static final String BEDROCK_ORE_ID = Constants.MOD_ID + ':' + Constants.NAME_BEDROCK_ORE;
    private static final String TYPO_BEDROCK_MINER_ID = Constants.MOD_ID + ": " + Constants.NAME_BEDROCK_MINER;
    private static final String BEDROCK_MINER_ID = Constants.MOD_ID + ':' + Constants.NAME_BEDROCK_MINER;

    @Override
    public int getFixVersion() {
        return 1;
    }

    @Override
    public NBTTagCompound fixTagCompound(final NBTTagCompound compound) {
        if (compound.hasKey(TAG_ID, NBT.TAG_STRING)) {
            final String id = compound.getString(TAG_ID);
            if (Objects.equals(id, TYPO_BEDROCK_ORE_ID)) {
                compound.setString(TAG_ID, BEDROCK_ORE_ID);
            } else if (Objects.equals(id, TYPO_BEDROCK_MINER_ID)) {
                compound.setString(TAG_ID, BEDROCK_MINER_ID);
            }
        }
        return compound;
    }
}
