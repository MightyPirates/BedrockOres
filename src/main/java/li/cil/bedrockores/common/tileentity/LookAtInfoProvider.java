package li.cil.bedrockores.common.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface LookAtInfoProvider {
    @SideOnly(Side.CLIENT)
    String getLookAtInfo();

    void updateLookAtInfo();
}
