package li.cil.bedrockores.client.render;

import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import static java.util.Objects.requireNonNull;

public record Ore(BlockState state, BakedModel model, ModelData data) {
    public static final ModelProperty<Ore> ORE_PROPERTY = new ModelProperty<>();

    public static Ore create(final BedrockOreBlockEntity blockEntity) {
        final var state = blockEntity.getOreBlockState();
        final var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        final var data = model.getModelData(requireNonNull(blockEntity.getLevel()), blockEntity.getBlockPos(), state, ModelData.EMPTY);
        return new Ore(state, model, data);
    }

    public ModelData asModelData() {
        return ModelData.builder()
                .with(Ore.ORE_PROPERTY, this)
                .build();
    }
}
