/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.item;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.Settings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * Carries the miner's area-size tooltip.
 * <p>
 * {@code Block#appendHoverText} no longer exists as of 1.21.11 — tooltips for blocks have to be
 * contributed by their item.
 */
public final class BedrockMinerBlockItem extends BlockItem {
    public BedrockMinerBlockItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display, final Consumer<Component> tooltip, final TooltipFlag flags) {
        super.appendHoverText(stack, context, display, tooltip, flags);

        final var edgeLength = (Settings.minerAreaRadius.get() - 1) * 2 + 1;
        final var layers = Settings.minerAreaLayers.get();
        tooltip.accept(Component.translatable(Constants.TOOLTIP_BEDROCK_MINER, edgeLength, layers, edgeLength).withStyle(ChatFormatting.GRAY));
    }
}
