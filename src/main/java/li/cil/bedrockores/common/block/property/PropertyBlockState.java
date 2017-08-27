package li.cil.bedrockores.common.block.property;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public final class PropertyBlockState implements IUnlistedProperty<IBlockState> {
    private final String name;

    // --------------------------------------------------------------------- //

    public PropertyBlockState(final String name) {
        this.name = name;
    }

    // --------------------------------------------------------------------- //
    // IUnlistedProperty

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(final IBlockState value) {
        return value != null;
    }

    @Override
    public Class<IBlockState> getType() {
        return IBlockState.class;
    }

    @Override
    public String valueToString(final IBlockState value) {
        return value.toString();
    }
}
