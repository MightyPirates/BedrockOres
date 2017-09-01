package li.cil.bedrockores.util.datafix;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.util.datafix.fixes.TileEntityTypo;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;

public final class Fixes {
    public static void init() {
        final ModFixs fixer = FMLCommonHandler.instance().getDataFixer().init(Constants.MOD_ID, 1);

        fixer.registerFix(FixTypes.BLOCK_ENTITY, new TileEntityTypo());
    }

    private Fixes() {
    }
}
