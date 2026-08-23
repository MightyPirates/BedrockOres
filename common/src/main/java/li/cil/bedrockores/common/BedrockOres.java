/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common;

import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import li.cil.bedrockores.common.block.BedrockOreBlock;
import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import li.cil.bedrockores.common.block.entity.UnconfiguredOreCleanup;
import li.cil.bedrockores.common.command.ModCommands;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.item.Items;
import li.cil.bedrockores.common.network.Network;
import li.cil.bedrockores.common.sound.Sounds;
import li.cil.bedrockores.common.world.BedrockOreFeatures;
import li.cil.bedrockores.common.world.BedrockOrePlacements;

public final class BedrockOres {
    public static void initialize() {
        Settings.initialize();

        Network.initialize();

        Blocks.initialize();
        BlockEntities.initialize();
        Items.initialize();
        Sounds.initialize();
        BedrockOreFeatures.initialize();
        UnconfiguredOreCleanup.initialize();

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> ModCommands.register(dispatcher));

        BlockEvent.BREAK.register(BedrockOreBlock::onBlockBreak);

        LifecycleEvent.SERVER_STARTED.register(BedrockOrePlacements::checkOverworldVeins);
    }

    private BedrockOres() {
    }
}
