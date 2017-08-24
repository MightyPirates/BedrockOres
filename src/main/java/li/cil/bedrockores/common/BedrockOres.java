package li.cil.bedrockores.common;

import li.cil.bedrockores.common.command.CommandBedrockOres;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.world.Retrogen;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MOD_ID, version = Constants.MOD_VERSION, name = Constants.MOD_NAME,
     useMetadata = true)
public final class BedrockOres {
    // --------------------------------------------------------------------- //
    // FML / Forge

    @Mod.Instance(Constants.MOD_ID)
    public static BedrockOres instance;

    @SidedProxy(clientSide = Constants.PROXY_CLIENT, serverSide = Constants.PROXY_COMMON)
    public static ProxyCommon proxy;

    @Mod.EventHandler
    public void onPreInit(final FMLPreInitializationEvent event) {
        log = event.getModLog();
        proxy.onPreInit(event);
    }

    @Mod.EventHandler
    public void onInit(final FMLInitializationEvent event) {
        proxy.onInit(event);
    }

    @Mod.EventHandler
    public void onPostInit(final FMLPostInitializationEvent event) {
        proxy.onPostInit(event);
    }

    @Mod.EventHandler
    public void serverAboutToStart(final FMLServerAboutToStartEvent evt) {
        Retrogen.INSTANCE.clear();
    }

    @Mod.EventHandler
    public void onServerStarting(final FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandBedrockOres());
    }

    // --------------------------------------------------------------------- //

    private static Logger log;

    public static Logger getLog() {
        return log;
    }
}
