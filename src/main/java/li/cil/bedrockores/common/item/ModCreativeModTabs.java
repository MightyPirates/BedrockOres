package li.cil.bedrockores.common.item;

import li.cil.bedrockores.common.config.Constants;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID)
public final class ModCreativeModTabs {
    @SubscribeEvent
    public static void handleCreativeTabEvent(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(Items.BEDROCK_MINER.get());
        }
    }
}
