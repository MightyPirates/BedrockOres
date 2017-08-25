package li.cil.bedrockores.common.block;

import li.cil.bedrockores.common.block.property.PropertyBlockState;
import li.cil.bedrockores.common.config.Settings;
import li.cil.bedrockores.common.tileentity.TileEntityBedrockOre;
import li.cil.bedrockores.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nullable;

/**
 * This block itself is configured like bedrock; however, it forwards pretty
 * much everything logic related to the underlying ore block, such as hardness
 * checks, harvesting (drops). It only does slightly custom rendering (a mask
 * over the underlying ore's model) and not actually breaking when harvested,
 * but instead reducing the remaining amount by one.
 */
public final class BlockBedrockOre extends Block {
    public static final PropertyBlockState ORE_BLOCK_STATE = new PropertyBlockState("ore");

    public BlockBedrockOre() {
        super(Material.ROCK);
        setBlockUnbreakable();
        setResistance(6000000);
        setSoundType(SoundType.STONE);
        disableStats();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{ORE_BLOCK_STATE});
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        // When rendering we want to use our own baked model for masked
        // rendering of the underlying ore; in all other instances we want to
        // behave like the underlying ore itself (e.g. for canHarvest checks).
        if (MinecraftForgeClient.getRenderLayer() == null) {
            final IBlockState oreBlockState = getOreBlockState(WorldUtils.getTileEntityThreadsafe(world, pos));
            if (oreBlockState != null) {
                return oreBlockState;
            }
        }
        return super.getActualState(state, world, pos);
    }

    @Override
    public IBlockState getExtendedState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
        if (oreBlockState != null) {
            return ((IExtendedBlockState) state).withProperty(ORE_BLOCK_STATE, oreBlockState);
        }
        return state;
    }

    @Override
    public boolean hasTileEntity(final IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return new TileEntityBedrockOre();
    }

    @Override
    public boolean canRenderInLayer(final IBlockState state, final BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean removedByPlayer(final IBlockState state, final World world, final BlockPos pos, final EntityPlayer player, final boolean willHarvest) {
        if (player.capabilities.isCreativeMode) {
            return super.removedByPlayer(state, world, pos, player, willHarvest);
        }

        if (!Settings.allowPlayerMining) {
            return false;
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityBedrockOre) {
            final TileEntityBedrockOre tileEntityBedrockOre = (TileEntityBedrockOre) tileEntity;

            // Ignore result, expect drops to be handled by underlying ore.
            tileEntityBedrockOre.extract();

            final IBlockState oreBlockState = tileEntityBedrockOre.getOreBlockState();
            if (oreBlockState != null) {
                oreBlockState.getBlock().onBlockHarvested(world, pos, oreBlockState, player);
            }
        }

        return true;
    }

    // --------------------------------------------------------------------- //
    // Forwarding to actual ore

    @Override
    public int getMetaFromState(final IBlockState state) {
        if (state.getBlock() != this) {
            return state.getBlock().getMetaFromState(state);
        }
        return super.getMetaFromState(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockHardness(final IBlockState state, final World world, final BlockPos pos) {
        if (Settings.allowPlayerMining) {
            final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
            if (oreBlockState != null) {
                return oreBlockState.getBlockHardness(world, pos);
            }
        }
        return super.getBlockHardness(state, world, pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getPlayerRelativeBlockHardness(final IBlockState state, final EntityPlayer player, final World world, final BlockPos pos) {
        if (Settings.allowPlayerMining) {
            final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
            if (oreBlockState != null) {
                return oreBlockState.getPlayerRelativeBlockHardness(player, world, pos);
            }
        }
        return super.getPlayerRelativeBlockHardness(state, player, world, pos);
    }

    @Override
    public boolean canHarvestBlock(final IBlockAccess world, final BlockPos pos, final EntityPlayer player) {
        if (Settings.allowPlayerMining) {
            final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
            if (oreBlockState != null) {
                return oreBlockState.getBlock().canHarvestBlock(world, pos, player);
            }
        }
        return super.canHarvestBlock(world, pos, player);
    }

    @Override
    public boolean canSilkHarvest(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player) {
        if (Settings.allowPlayerMining) {
            final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
            if (oreBlockState != null) {
                return oreBlockState.getBlock().canSilkHarvest(world, pos, oreBlockState, player);
            }
        }
        return super.canSilkHarvest(world, pos, state, player);
    }

    @Override
    public int getExpDrop(final IBlockState state, final IBlockAccess world, final BlockPos pos, final int fortune) {
        if (Settings.allowPlayerMining) {
            final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
            if (oreBlockState != null) {
                return oreBlockState.getBlock().getExpDrop(oreBlockState, world, pos, fortune);
            }
        }
        return super.getExpDrop(state, world, pos, fortune);
    }

    @Override
    public void harvestBlock(final World world, final EntityPlayer player, final BlockPos pos, final IBlockState state, @Nullable final TileEntity te, @Nullable final ItemStack stack) {
        if (Settings.allowPlayerMining) {
            final IBlockState oreBlockState = getOreBlockState(te);
            if (oreBlockState != null) {
                oreBlockState.getBlock().harvestBlock(world, player, pos, oreBlockState, null, stack);
            }
        }
    }

    @Override
    public SoundType getSoundType(final IBlockState state, final World world, final BlockPos pos, @Nullable final Entity entity) {
        final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
        if (oreBlockState != null) {
            return oreBlockState.getBlock().getSoundType(oreBlockState, world, pos, entity);
        }
        return super.getSoundType(state, world, pos, entity);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getLightValue(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
        if (oreBlockState != null) {
            // Not the state sensitive one because that leads to a recursive
            // loop due to a `this == world.getBlockState().getBlock()` check
            // in the parent implementation.
            return oreBlockState.getLightValue();
        }
        return super.getLightValue(state, world, pos);
    }

    @Override
    public boolean canCreatureSpawn(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EntityLiving.SpawnPlacementType type) {
        final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
        if (oreBlockState != null) {
            return oreBlockState.getBlock().canCreatureSpawn(oreBlockState, world, pos, type);
        }
        return super.canCreatureSpawn(state, world, pos, type);
    }

    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player) {
        final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
        if (oreBlockState != null) {
            return oreBlockState.getBlock().getPickBlock(oreBlockState, target, world, pos, player);
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public boolean addHitEffects(final IBlockState state, final World world, final RayTraceResult target, final ParticleManager manager) {
        final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(target.getBlockPos()));
        if (oreBlockState != null) {
            return oreBlockState.getBlock().addHitEffects(oreBlockState, world, target, manager);
        }
        return super.addHitEffects(state, world, target, manager);
    }

    @Override
    public boolean addDestroyEffects(final World world, final BlockPos pos, final ParticleManager manager) {
        final IBlockState oreBlockState = getOreBlockState(world.getTileEntity(pos));
        if (oreBlockState != null) {
            return oreBlockState.getBlock().addDestroyEffects(world, pos, manager);
        }
        return super.addDestroyEffects(world, pos, manager);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getPackedLightmapCoords(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        final IBlockState oreBlockState = getOreBlockState(source.getTileEntity(pos));
        if (oreBlockState != null) {
            return oreBlockState.getBlock().getPackedLightmapCoords(oreBlockState, source, pos);
        }
        return super.getPackedLightmapCoords(state, source, pos);
    }

    // --------------------------------------------------------------------- //

    @Nullable
    private static IBlockState getOreBlockState(@Nullable final TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityBedrockOre) {
            final TileEntityBedrockOre tileEntityBedrockOre = (TileEntityBedrockOre) tileEntity;
            return tileEntityBedrockOre.getOreBlockState();
        }
        return null;
    }
}
