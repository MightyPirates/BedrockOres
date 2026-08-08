package li.cil.bedrockores.common.util.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public final class InventoryUtilsImpl {
    @Nullable
    public static ItemStack insert(final Level level, final BlockPos pos, final Direction side, final ItemStack stack) {
        final var handler = findHandler(level, pos, side);
        if (handler == null) {
            return null;
        }

        try (final var transaction = Transaction.openRoot()) {
            final var remainder = ItemUtil.insertItemReturnRemaining(handler, stack, false, transaction);
            transaction.commit();
            return remainder;
        }
    }

    @Nullable
    private static ResourceHandler<ItemResource> findHandler(final Level level, final BlockPos pos, final Direction side) {
        final var blockHandler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        if (blockHandler != null) {
            return blockHandler;
        }

        final var entities = level.getEntities((Entity) null, new AABB(pos),
                entity -> entity.getCapability(Capabilities.Item.ENTITY_AUTOMATION, side) != null);
        if (entities.isEmpty()) {
            return null;
        }

        final var entity = entities.get(level.random.nextInt(entities.size()));
        return entity.getCapability(Capabilities.Item.ENTITY_AUTOMATION, side);
    }

    private InventoryUtilsImpl() {
    }
}
