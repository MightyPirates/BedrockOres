/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.common.util.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public final class InventoryUtilsImpl {
    @Nullable
    public static ItemStack insert(final Level level, final BlockPos pos, final Direction side, final ItemStack stack) {
        final var storage = findStorage(level, pos, side);
        if (storage == null) {
            return null;
        }

        final long inserted;
        try (var transaction = Transaction.openOuter()) {
            inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            transaction.commit();
        }

        final var remainder = stack.copy();
        remainder.shrink((int) inserted);
        return remainder;
    }

    @Nullable
    private static Storage<ItemVariant> findStorage(final Level level, final BlockPos pos, final Direction side) {
        final var blockStorage = ItemStorage.SIDED.find(level, pos, side);
        if (blockStorage != null) {
            return blockStorage;
        }

        // Chest minecarts and the like; the Transfer API's sided lookup is block-only.
        final var entities = level.getEntities((Entity) null, new AABB(pos), entity -> entity instanceof Container);
        if (entities.isEmpty()) {
            return null;
        }

        final var entity = entities.get(level.random.nextInt(entities.size()));
        return InventoryStorage.of((Container) entity, side);
    }

    private InventoryUtilsImpl() {
    }
}
