package li.cil.bedrockores.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.server.command.EnumArgument;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class ModCommands {
    private enum BedrockOreOperation {
        WRAP,
        UNWRAP,
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bedrock_ores")
                .requires(stack -> stack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.argument("wrap", EnumArgument.enumArgument(BedrockOreOperation.class))
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(context -> {
                        final var operation = context.getArgument("wrap", BedrockOreOperation.class);
                        final var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                        final var level = context.getSource().getLevel();

                        switch (operation) {
                            case WRAP -> {
                                final var state = level.getBlockState(pos);
                                level.setBlock(pos, Blocks.BEDROCK_ORE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                                if (level.getBlockEntity(pos) instanceof BedrockOreBlockEntity bedrockOre) {
                                    bedrockOre.setOreBlockState(state);
                                    bedrockOre.setAmount(1);
                                }
                            }
                            case UNWRAP -> {
                                if (level.getBlockEntity(pos) instanceof BedrockOreBlockEntity bedrockOre) {
                                    final var state = bedrockOre.getOreBlockState();
                                    level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                                }
                            }
                        }

                        return SINGLE_SUCCESS;
                    })))

                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(context -> {
                        final var amount = IntegerArgumentType.getInteger(context, "amount");
                        final var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                        final var level = context.getSource().getLevel();

                        if (level.getBlockEntity(pos) instanceof BedrockOreBlockEntity bedrockOre) {
                            bedrockOre.setAmount(amount);
                        }

                        return SINGLE_SUCCESS;
                    }))));
    }
}
