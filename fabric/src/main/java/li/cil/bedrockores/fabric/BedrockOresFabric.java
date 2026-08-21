/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.fabric;

import li.cil.bedrockores.common.BedrockOres;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import li.cil.bedrockores.common.world.BedrockOrePlacements;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.levelgen.GenerationStep;
import team.reborn.energy.api.EnergyStorage;

public final class BedrockOresFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BedrockOres.initialize();

        for (final var placement : BedrockOrePlacements.OVERWORLD) {
            BiomeModifications.addFeature(
                    BiomeSelectors.foundInOverworld(),
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    placement);
        }

        EnergyStorage.SIDED.registerForBlockEntity(
                (miner, side) -> side != null && side.getAxis().isHorizontal() && miner.getEnergyCapacity() > 0
                        ? new MinerEnergyStorage(miner)
                        : null,
                BlockEntities.MINER.get());
    }
}
