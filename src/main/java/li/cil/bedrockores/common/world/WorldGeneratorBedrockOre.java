package li.cil.bedrockores.common.world;

import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import li.cil.bedrockores.common.config.OreConfigManager;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.config.ore.OreConfig;
import li.cil.bedrockores.common.init.Blocks;
import li.cil.bedrockores.common.tileentity.TileEntityBedrockOre;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum WorldGeneratorBedrockOre implements IWorldGenerator {
    INSTANCE;

    // --------------------------------------------------------------------- //

    @Nullable
    @GameRegistry.ObjectHolder("minecraft:bedrock")
    public static final Block bedrock = null;

    @Nullable
    @GameRegistry.ObjectHolder("bedrockbgone:better_bedrock")
    public static final Block betterBedrock = null;

    // --------------------------------------------------------------------- //

    @Override
    public void generate(final Random random, final int chunkX, final int chunkZ, final World world, final IChunkGenerator chunkGenerator, final IChunkProvider chunkProvider) {
        if (Retrogen.INSTANCE.markChunkGenerated(world.provider.getDimension(), chunkX, chunkZ)) {
            generateImpl(random, chunkX, chunkZ, world);
        }
        world.getChunk(chunkX, chunkZ).markDirty();
    }

    // --------------------------------------------------------------------- //

    private static void generateImpl(final Random random, final int chunkX, final int chunkZ, final World world) {
        if (random.nextFloat() >= Settings.veinChance) {
            return;
        }

        final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        final OreConfig ore = OreConfigManager.INSTANCE.getOre(world, chunkPos, random);
        if (ore == null) {
            return;
        }

        final int veinMinWidth = MathHelper.clamp(ore.widthMin, 1, 15);
        final int veinMaxWidth = MathHelper.clamp(ore.widthMax, veinMinWidth, 15);
        final int veinMinHeight = MathHelper.clamp(ore.heightMin, 1, 255);
        final int veinMaxHeight = MathHelper.clamp(veinMinHeight, ore.heightMax, 255);
        final int veinMinCount = MathHelper.clamp(ore.countMin, 1, 8 * 8 * 255);
        final int veinMaxCount = MathHelper.clamp(veinMinCount, ore.countMax, 8 * 8 * 255);
        final int veinMinYield = Math.max(1, ore.yieldMin);
        final int veinMaxYield = Math.max(veinMinYield, ore.yieldMax);

        final int veinCount = veinMinCount == veinMaxCount ? veinMinCount : (veinMinCount + random.nextInt(veinMaxCount - veinMinCount));
        if (veinCount == 0) {
            return;
        }

        final float yieldScale = Settings.veinYieldConstScale * OreConfigManager.INSTANCE.getOreProvider(world, chunkPos).getOres().size();
        final int veinYield = Math.max(0, Math.round((veinMinYield == veinMaxYield ? veinMinYield : (veinMinYield + random.nextInt(veinMaxYield - veinMinYield))) * yieldScale));
        if (veinYield == 0) {
            return;
        }

        // We generate veins in ellipsoid shapes in the bedrock. Pick a width
        // and height for the ellipse, as well as a center.
        final float a = (veinMinWidth == veinMaxWidth ? veinMinWidth : (veinMinWidth + random.nextInt(veinMaxWidth - veinMinWidth + 1))) / 2f;
        final float b = (veinMinWidth == veinMaxWidth ? veinMinWidth : (veinMinWidth + random.nextInt(veinMaxWidth - veinMinWidth + 1))) / 2f;
        final float maxWidth = Math.max(a, b);
        final int h = veinMinHeight + random.nextInt(veinMaxHeight - veinMinHeight);
        final float rotation = random.nextFloat() * (float) Math.PI;
        final float centerX = chunkX * 16 + 8 + maxWidth + random.nextInt(16 - Math.round(maxWidth * 2) + 1);
        final float centerZ = chunkZ * 16 + 8 + maxWidth + random.nextInt(16 - Math.round(maxWidth * 2) + 1);

        final double distanceToSpawn;
        if (world.provider.canRespawnHere()) {
            if (world instanceof WorldServer && ((WorldServer) world).findingSpawnPoint) {
                // If this is called *while* we're looking for a valid spawn pos, the reported
                // spawn pos will be BlockPos.ORIGIN. So we're almost guaranteed to be far enough
                // away from it for scaling to kick in. Inversely, if we're looking for a spawn pos,
                // we assume we're close enough to the spawn for scaling *not* to kick in.
                distanceToSpawn = 0;
            } else {
                final BlockPos spawnPoint = world.getSpawnPoint();
                distanceToSpawn = new Vec3i(spawnPoint.getX(), 0, spawnPoint.getZ()).getDistance(MathHelper.floor(centerX), 0, MathHelper.floor(centerZ));
            }
        } else {
            distanceToSpawn = Settings.veinDistanceScaleMultiplier;
        }

        if (distanceToSpawn < Settings.veinMinSpawnDistance) {
            return;
        }

        final float veinScale;
        if (distanceToSpawn > Settings.veinDistanceScaleStart) {
            veinScale = Math.max(1, (float) Math.log((distanceToSpawn - Settings.veinDistanceScaleStart) / 10) * Settings.veinDistanceScaleMultiplier);
        } else {
            veinScale = 1;
        }

        final int adjustedCount = Math.round(veinCount * Math.max(1, veinScale * 0.5f));
        final int adjustedYield = Math.round(veinYield * veinScale);

        final int minX = MathHelper.ceil(centerX - maxWidth);
        final int maxX = MathHelper.floor(centerX + maxWidth) - 1;
        final int minZ = MathHelper.ceil(centerZ - maxWidth);
        final int maxZ = MathHelper.floor(centerZ + maxWidth) - 1;

        final List<BlockPos> candidates = new ArrayList<>();
        final TFloatList distribution = new TFloatArrayList();

        assert candidates.isEmpty();
        assert distribution.isEmpty();

        // Pick all candidate positions in the target bounds, those positions
        // being the ones that fall inside our ellipsoid.
        int maxY = 0;
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                if (!isPointInEllipse(x, z, centerX, centerZ, a, b, rotation)) {
                    continue;
                }
                for (int y = Settings.veinBaseY; y >= 0; y--) {
                    final BlockPos pos = new BlockPos(x, y, z);
                    final IBlockState state = world.getBlockState(pos);
                    assert state.getBlock() != Blocks.bedrockOre;
                    if (state.getBlock().isReplaceableOreGen(state, world, pos, WorldGeneratorBedrockOre::isBedrockBlock)) {
                        if (y > maxY) {
                            maxY = y;
                        }
                        candidates.add(pos);
                    }
                }
            }
        }

        // We start at the typical max y-level for bedrock, but in case we
        // don't find anything at the higher levels make sure we still try
        // to use the full height.
        final int minY = maxY - h;
        candidates.removeIf(pos -> pos.getY() <= minY);

        if (candidates.size() == 0) {
            return;
        }

        // Inside the ellipsoid we pick a number of actually used blocks
        // in a uniform random fashion.
        if (candidates.size() > adjustedCount) {
            Collections.shuffle(candidates, random);
        }

        final int placeCount = Math.min(adjustedCount, candidates.size());

        // Each generated block gets a bit of randomness to its actual
        // amount to make things less boring.
        float sum = 0;
        for (int i = 0; i < placeCount; i++) {
            final float weight = random.nextFloat();
            sum += weight;
            distribution.add(weight);
        }

        // Half of the total yield is evenly distributed across blocks, the
        // rest falls into this random distribution. Adjust the normalizer
        // accordingly.
        final float fixedYield = adjustedYield / 2f;
        final int baseYield = MathHelper.ceil(fixedYield / placeCount);
        final float normalizer = (adjustedYield - fixedYield) / sum;
        int remaining = adjustedYield;
        for (int i = 0; i < placeCount && remaining > 0; i++) {
            final int amount = Math.min(remaining, baseYield + MathHelper.ceil(distribution.get(i) * normalizer));
            if (amount == 0) {
                continue;
            }

            remaining -= amount;

            final BlockPos pos = candidates.get(i);
            world.setBlockState(pos, Blocks.bedrockOre.getDefaultState(), 2);

            final TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityBedrockOre) {
                final TileEntityBedrockOre tileEntityBedrockOre = (TileEntityBedrockOre) tileEntity;
                tileEntityBedrockOre.setOreBlockState(ore.state.getBlockState(), amount);
            }
        }

        assert remaining == 0;
    }

    private static boolean isPointInEllipse(final float px, final float py, final float ex, final float ey, final float ea, final float eb, final float er) {
        final float cr = MathHelper.cos(er);
        final float sr = MathHelper.sin(er);

        final float dx = px - ex;
        final float dy = py - ey;

        final float leftTop = cr * dx + sr * dy;
        final float left = (leftTop * leftTop) / (ea * ea);

        final float rightTop = sr * dx - cr * dy;
        final float right = (rightTop * rightTop) / (eb * eb);

        return left + right <= 1;
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean isBedrockBlock(@Nullable final IBlockState input) {
        if (input == null) {
            return false;
        }
        final Block block = input.getBlock();
        return block == bedrock || block == betterBedrock;
    }
}
