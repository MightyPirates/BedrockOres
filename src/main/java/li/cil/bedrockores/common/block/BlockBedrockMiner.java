package li.cil.bedrockores.common.block;

import li.cil.bedrockores.common.config.Constants;
import li.cil.bedrockores.common.tileentity.TileEntityBedrockMiner;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public final class BlockBedrockMiner extends Block {
    public BlockBedrockMiner() {
        super(Material.IRON);
        setHardness(5);
        setResistance(10);
        setSoundType(SoundType.METAL);
    }

    // --------------------------------------------------------------------- //
    // Block

    @Override
    public boolean hasTileEntity(final IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return new TileEntityBedrockMiner();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(final IBlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(final IBlockState blockState, final World world, final BlockPos pos) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityBedrockMiner) {
            return ((TileEntityBedrockMiner) tileEntity).isWorking() ? 15 : 0;
        }
        return super.getComparatorInputOverride(blockState, world, pos);
    }

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        final TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity != null) {
            final IItemHandler itemHandler = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (itemHandler != null) {
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemHandler.getStackInSlot(slot));
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List<String> tooltip, final boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(new TextComponentTranslation(Constants.TOOLTIP_BEDROCK_MINER).getFormattedText());
    }
}
