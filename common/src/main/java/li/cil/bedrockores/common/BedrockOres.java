/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common;

import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import li.cil.bedrockores.common.block.BedrockOreBlock;
import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import li.cil.bedrockores.common.command.ModCommands;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.item.Items;
import li.cil.bedrockores.common.network.Network;
import li.cil.bedrockores.common.sound.Sounds;
import li.cil.bedrockores.common.world.BedrockOreFeatures;

public final class BedrockOres {
    public static void initialize() {
        Settings.initialize();

        Network.initialize();

        Blocks.initialize();
        BlockEntities.initialize();
        Items.initialize();
        Sounds.initialize();
        BedrockOreFeatures.initialize();

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> ModCommands.register(dispatcher));

        BlockEvent.BREAK.register(BedrockOreBlock::onBlockBreak);
    }

    private BedrockOres() {
    }
}
