/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.fabric;

import li.cil.bedrockores.common.BedrockOres;
import li.cil.bedrockores.common.block.entity.BlockEntities;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.world.BedrockOrePlacements;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import team.reborn.energy.api.EnergyStorage;

public final class BedrockOresFabric implements ModInitializer {
    private Registry<PlacedFeature> placedFeatures;

    @Override
    public void onInitialize() {
        BedrockOres.initialize();

        DynamicRegistrySetupCallback.EVENT.register(registries ->
                registries.getOptional(Registries.PLACED_FEATURE).ifPresent(registry -> placedFeatures = registry));

        BiomeModifications
                .create(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "overworld_veins"))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.tag(BiomeTags.IS_OVERWORLD), context -> {
                    for (final var vein : placedFeatures.getTagOrEmpty(BedrockOrePlacements.OVERWORLD_VEINS)) {
                        context.getGenerationSettings().addFeature(
                                GenerationStep.Decoration.UNDERGROUND_ORES,
                                vein.unwrapKey().orElseThrow());
                    }
                });

        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                BedrockOrePlacements.UNCONFIGURED_ORE_CLEANUP);

        EnergyStorage.SIDED.registerForBlockEntity(
                (miner, side) -> side != null && side.getAxis().isHorizontal() && miner.getEnergyCapacity() > 0
                        ? new MinerEnergyStorage(miner)
                        : null,
                BlockEntities.MINER.get());
    }
}
