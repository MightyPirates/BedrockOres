package li.cil.bedrockores.common;

import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import li.cil.bedrockores.common.command.ModCommands;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.item.Items;
import li.cil.bedrockores.common.network.Network;
import li.cil.bedrockores.common.sound.Sounds;
import li.cil.bedrockores.common.world.BedrockOreFeatures;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public final class BedrockOres {
    public BedrockOres() {
        Settings.initialize();

        Network.initialize();

        Blocks.initialize();
        BlockEntities.initialize();
        Items.initialize();
        Sounds.initialize();
        BedrockOreFeatures.initialize();

        MinecraftForge.EVENT_BUS.addListener(BedrockOres::onCommandsRegister);
    }

    private static void onCommandsRegister(final RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
