package li.cil.bedrockores.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import li.cil.bedrockores.common.block.BedrockOreBlock;
import li.cil.bedrockores.common.block.Blocks;
import li.cil.bedrockores.common.block.entity.BedrockOreBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.world.level.block.Block;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class ModCommands {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bedrock_ores")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))

                .then(Commands.literal("wrap")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ModCommands::wrap)))

                .then(Commands.literal("unwrap")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ModCommands::unwrap)))

                .then(Commands.literal("amount")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ModCommands::amount)))));
    }

    // --------------------------------------------------------------------- //

    private static int wrap(final CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        final var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        final var level = context.getSource().getLevel();

        final var state = level.getBlockState(pos);
        level.setBlock(pos, Blocks.BEDROCK_ORE.get().defaultBlockState()
                .setValue(BedrockOreBlock.LIGHT, state.getLightEmission()), Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(pos) instanceof final BedrockOreBlockEntity bedrockOre) {
            bedrockOre.setOreBlockState(state);
            bedrockOre.setAmount(1);
        }

        return SINGLE_SUCCESS;
    }

    private static int unwrap(final CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        final var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        final var level = context.getSource().getLevel();

        if (level.getBlockEntity(pos) instanceof final BedrockOreBlockEntity bedrockOre) {
            final var state = bedrockOre.getOreBlockState();
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }

        return SINGLE_SUCCESS;
    }

    private static int amount(final CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        final var amount = IntegerArgumentType.getInteger(context, "amount");
        final var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        final var level = context.getSource().getLevel();

        if (level.getBlockEntity(pos) instanceof final BedrockOreBlockEntity bedrockOre) {
            bedrockOre.setAmount(amount);
        }

        return SINGLE_SUCCESS;
    }

    private ModCommands() {
    }
}
