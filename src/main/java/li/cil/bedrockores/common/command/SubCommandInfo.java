package li.cil.bedrockores.common.command;

import joptsimple.internal.Strings;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.ore.OreConfig;
import li.cil.bedrockores.common.config.OreConfigManager;
import li.cil.bedrockores.util.BiomeUtils;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Locale;
import java.util.stream.Collectors;

final class SubCommandInfo extends AbstractSubCommand {
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) {
        final World world = sender.getEntityWorld();
        final Biome biome = world.getBiome(sender.getPosition());

        final String dimensionId = String.valueOf(world.provider.getDimension());
        final String dimensionType = world.provider.getDimensionType().getName().toLowerCase(Locale.US);
        final String biomeId = biome.getRegistryName().toString();
        final String biomeTypes = String.join(", ", BiomeUtils.getBiomeTypes(biome).stream().map(e -> e.name().toLowerCase(Locale.US)).collect(Collectors.toList()));

        notifyCommandListener(sender, this, Constants.COMMAND_INFO, dimensionId, dimensionType, biomeId, Strings.isNullOrEmpty(biomeTypes) ? "?" : biomeTypes);
        for (final OreConfig ore : OreConfigManager.INSTANCE.getOres(world, new ChunkPos(sender.getPosition()))) {
            notifyCommandListener(sender, this, Constants.COMMAND_LIST_ITEM, ore.state.getBlockState().toString());
        }
    }
}
