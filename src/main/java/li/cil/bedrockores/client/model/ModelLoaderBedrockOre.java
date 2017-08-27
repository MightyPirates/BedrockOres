package li.cil.bedrockores.client.model;

import li.cil.bedrockores.client.render.ModelBedrockOre;
import li.cil.bedrockores.common.config.Constants;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum ModelLoaderBedrockOre implements ICustomModelLoader {
    INSTANCE;

    // --------------------------------------------------------------------- //

    private final ResourceLocation BEDROCK_ORE_LOCATION = new ResourceLocation(Constants.MOD_ID, Constants.NAME_BEDROCK_ORE);
    private final ResourceLocation BEDROCK_ORE_MASK_LOCATION = new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID, Constants.NAME_BEDROCK_ORE + "_mask"), "normal");

    private IModel maskModel;

    // --------------------------------------------------------------------- //

    public IModel getMaskModel() {
        return maskModel;
    }

    // --------------------------------------------------------------------- //
    // ICustomModelLoader

    @Override
    public boolean accepts(final ResourceLocation modelLocation) {
        return BEDROCK_ORE_LOCATION.equals(modelLocation);
    }

    @Override
    public IModel loadModel(final ResourceLocation modelLocation) throws Exception {
        maskModel = ModelLoaderRegistry.getModel(BEDROCK_ORE_MASK_LOCATION);

        return ModelBedrockOre.INSTANCE;
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {
    }
}
