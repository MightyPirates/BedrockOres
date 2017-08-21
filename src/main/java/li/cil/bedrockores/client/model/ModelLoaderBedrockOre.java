package li.cil.bedrockores.client.model;

import li.cil.bedrockores.client.render.ModelBedrockOre;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

@SideOnly(Side.CLIENT)
public enum ModelLoaderBedrockOre implements ICustomModelLoader {
    INSTANCE;

    public static final ResourceLocation MASK_LOCATION = new ResourceLocation(Constants.MOD_ID, "block/bedrock_ore_mask");

    private IModel maskModel;

    public IModel getMaskModel() {
        return maskModel;
    }

    @Override
    public boolean accepts(final ResourceLocation modelLocation) {
        return Objects.equals(modelLocation.getResourceDomain(), Constants.MOD_ID) &&
               Objects.equals(modelLocation.getResourcePath(), Constants.NAME_BEDROCK_ORE);
    }

    @Override
    public IModel loadModel(final ResourceLocation modelLocation) throws Exception {
        maskModel = ModelLoaderRegistry.getModelOrMissing(MASK_LOCATION);
        return ModelBedrockOre.INSTANCE;
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {
    }
}
