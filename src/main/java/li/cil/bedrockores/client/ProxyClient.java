package li.cil.bedrockores.client;

import li.cil.bedrockores.client.model.ModelLoaderBedrockOre;
import li.cil.bedrockores.client.render.LookAtInfoRenderer;
import li.cil.bedrockores.common.ProxyCommon;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.init.Items;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Takes care of client-side only setup.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class ProxyClient extends ProxyCommon {
    @Override
    public void onPreInit(final FMLPreInitializationEvent event) {
        super.onPreInit(event);

        ModelLoaderRegistry.registerLoader(ModelLoaderBedrockOre.INSTANCE);
    }

    @Override
    public void onInit(final FMLInitializationEvent event) {
        super.onInit(event);

        MinecraftForge.EVENT_BUS.register(LookAtInfoRenderer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(this);
    }

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public static void handleModelRegistryEvent(final ModelRegistryEvent event) {
        setCustomItemModelResourceLocation(Items.bedrockMiner);
    }

    @SubscribeEvent
    public void handleConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (ObjectUtils.notEqual(Constants.MOD_ID, event.getModID())) {
            return;
        }

        ConfigManager.sync(Constants.MOD_ID, Config.Type.INSTANCE);
    }

    // --------------------------------------------------------------------- //

    private static void setCustomItemModelResourceLocation(final Item item) {
        final ResourceLocation registryName = item.getRegistryName();
        assert registryName != null;
        final ModelResourceLocation location = new ModelResourceLocation(registryName, "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, location);
    }
}
