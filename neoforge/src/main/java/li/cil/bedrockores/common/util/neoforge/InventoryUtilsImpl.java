package li.cil.bedrockores.common.util.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

public final class InventoryUtilsImpl {
    @Nullable
    public static ItemStack insert(final Level level, final BlockPos pos, final Direction side, final ItemStack stack) {
        final var itemHandler = findItemHandler(level, pos, side);
        if (itemHandler == null) {
            return null;
        }
        return ItemHandlerHelper.insertItem(itemHandler, stack, false);
    }

    @Nullable
    private static IItemHandler findItemHandler(final Level level, final BlockPos pos, final Direction side) {
        final var blockHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (blockHandler != null) {
            return blockHandler;
        }

        final var entities = level.getEntities((Entity) null, new AABB(pos),
                entity -> entity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, side) != null);
        if (entities.isEmpty()) {
            return null;
        }

        final var entity = entities.get(level.random.nextInt(entities.size()));
        return entity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, side);
    }

    private InventoryUtilsImpl() {
    }
}
