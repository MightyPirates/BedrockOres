package li.cil.bedrockores.common.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.config.Settings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public enum Retrogen {
    INSTANCE;

    // --------------------------------------------------------------------- //

    private static final String BEDROCK_ORES_TAG = Constants.MOD_ID;
    private static final String GENERATED_TAG = "generated";

    private final TIntObjectMap<Set<ChunkPos>> worldGenPending = new TIntObjectHashMap<>(3);
    private final TIntObjectMap<Set<ChunkPos>> worldGenComplete = new TIntObjectHashMap<>(3);
    private final Object lock = new Object();

    // --------------------------------------------------------------------- //

    public void clear() {
        worldGenPending.clear();
        worldGenComplete.clear();
    }

    boolean markChunkGenerated(final int dimension, final int chunkX, final int chunkZ) {
        return put(worldGenComplete, dimension, new ChunkPos(chunkX, chunkZ));
    }

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public void handleChunkLoadEvent(final ChunkDataEvent.Load event) {
        final int dimension = event.getWorld().provider.getDimension();
        final ChunkPos chunkPos = event.getChunk().getPos();

        if (event.getData().getCompoundTag(BEDROCK_ORES_TAG).getBoolean(GENERATED_TAG)) {
            markChunkGenerated(dimension, chunkPos.x, chunkPos.z); // Store for next save.
            return;
        }

        put(worldGenPending, dimension, chunkPos);
    }

    @SubscribeEvent
    public void handleChunkSaveEvent(final ChunkDataEvent.Save event) {
        final int dimension = event.getWorld().provider.getDimension();
        final ChunkPos chunkPos = event.getChunk().getPos();

        if (!isComplete(dimension, chunkPos)) {
            return;
        }

        final NBTTagCompound compound = event.getData().getCompoundTag(BEDROCK_ORES_TAG);
        compound.setBoolean(GENERATED_TAG, true);
        event.getData().setTag(BEDROCK_ORES_TAG, compound);
    }

    @SubscribeEvent
    public void tickEnd(final TickEvent.WorldTickEvent event) {
        if (event.side != Side.SERVER) {
            return;
        }

        final World world = event.world;
        final int dimension = world.provider.getDimension();

        if (event.phase == TickEvent.Phase.END) {
            final Set<ChunkPos> pending = worldGenPending.get(dimension);
            if (pending == null) {
                return;
            }

            final Iterable<ChunkPos> chunks = ImmutableList.copyOf(Iterables.limit(pending, Settings.retrogenSpeed));
            for (final ChunkPos chunkPos : chunks) {
                // Seeding chunk RNG, see GameRegistry.generateWorld.
                final int chunkX = chunkPos.x;
                final int chunkZ = chunkPos.z;
                final long worldSeed = world.getSeed();
                final Random random = new Random(worldSeed);
                final long xSeed = random.nextLong() >> 2 + 1L;
                final long zSeed = random.nextLong() >> 2 + 1L;
                final long chunkSeed = (xSeed * chunkX + zSeed * chunkZ) ^ worldSeed;

                random.setSeed(chunkSeed);
                WorldGeneratorBedrockOre.INSTANCE.generate(random, chunkX, chunkZ, world, world.provider.createChunkGenerator(), world.getChunkProvider());

                pending.remove(chunkPos);
            }

            if (pending.isEmpty()) {
                worldGenPending.remove(dimension);
            }
        }
    }

    // --------------------------------------------------------------------- //

    private boolean put(final TIntObjectMap<Set<ChunkPos>> map, final int dimension, final ChunkPos chunkPos) {
        synchronized (lock) {
            Set<ChunkPos> complete = map.get(dimension);
            if (complete == null) {
                complete = new HashSet<>(128);
                map.put(dimension, complete);
            }
            return complete.add(chunkPos);
        }
    }

    private boolean isComplete(final int dimension, final ChunkPos chunkPos) {
        synchronized (lock) {
            final Set<ChunkPos> complete = worldGenComplete.get(dimension);
            return complete != null && complete.contains(chunkPos);
        }
    }
}