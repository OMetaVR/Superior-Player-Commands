package com.superiorplayercommands.command.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.block.*;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;

public class GrowCommand {
    
    private static final int DEFAULT_RADIUS = 10;
    private static final int MAX_RADIUS = 64;
    
    private static final Set<String> CROP_TYPES = Set.of(
        "wheat", "carrots", "carrot", "potatoes", "potato", "beetroots", "beetroot", "beet",
        "melon", "pumpkin", "cocoa", "nether_wart", "netherwart"
    );
    
    private static final Set<String> PLANT_TYPES = Set.of(
        "sapling", "saplings", "tree", "trees",
        "seed", "seeds", "crop", "crops",
        "all"
    );
    
    private static final SuggestionProvider<ServerCommandSource> TYPE_SUGGESTIONS = (context, builder) -> {
        return CommandSource.suggestMatching(
            List.of("wheat", "carrot", "potato", "beetroot", "melon", "pumpkin", 
                "sapling", "seed", "crop", "all"),
            builder
        );
    };
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("grow")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> execute(context, DEFAULT_RADIUS, "all"))
                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                    .executes(context -> execute(context, 
                        IntegerArgumentType.getInteger(context, "radius"), "all"))
                    .then(CommandManager.argument("type", StringArgumentType.word())
                        .suggests(TYPE_SUGGESTIONS)
                        .executes(context -> execute(context,
                            IntegerArgumentType.getInteger(context, "radius"),
                            StringArgumentType.getString(context, "type")))))
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context, int radius, String type) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = player.getServerWorld();
        BlockPos center = player.getBlockPos();
        
        int grown = 0;
        String typeLower = type.toLowerCase();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    
                    if (shouldGrow(block, typeLower)) {
                        if (growBlock(world, pos, state, block)) {
                            grown++;
                        }
                    }
                }
            }
        }
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            final int count = grown;
            final String finalType = type;
            if (count > 0) {
                source.sendFeedback(() -> Text.literal("Grew ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(String.valueOf(count))
                        .formatted(Formatting.AQUA))
                    .append(Text.literal(" plants")
                        .formatted(Formatting.GREEN)), false);
            } else {
                source.sendFeedback(() -> Text.literal("No growable plants found within " + radius + " blocks")
                    .formatted(Formatting.GRAY), false);
            }
        }
        
        return grown;
    }
    
    private static boolean shouldGrow(Block block, String type) {
        if (type.equals("all")) {
            return isGrowable(block);
        }
        
        if (type.equals("sapling") || type.equals("saplings") || type.equals("tree") || type.equals("trees")) {
            return block instanceof SaplingBlock;
        }
        
        if (type.equals("seed") || type.equals("seeds") || type.equals("crop") || type.equals("crops")) {
            return block instanceof CropBlock;
        }
        
        if (type.equals("wheat")) return block instanceof CropBlock && block == Blocks.WHEAT;
        if (type.equals("carrot") || type.equals("carrots")) return block == Blocks.CARROTS;
        if (type.equals("potato") || type.equals("potatoes")) return block == Blocks.POTATOES;
        if (type.equals("beetroot") || type.equals("beetroots") || type.equals("beet")) return block == Blocks.BEETROOTS;
        if (type.equals("melon")) return block == Blocks.MELON_STEM || block == Blocks.ATTACHED_MELON_STEM;
        if (type.equals("pumpkin")) return block == Blocks.PUMPKIN_STEM || block == Blocks.ATTACHED_PUMPKIN_STEM;
        if (type.equals("cocoa")) return block instanceof CocoaBlock;
        if (type.equals("nether_wart") || type.equals("netherwart")) return block == Blocks.NETHER_WART;
        
        return false;
    }
    
    private static boolean isGrowable(Block block) {
        return block instanceof CropBlock ||
               block instanceof SaplingBlock ||
               block instanceof StemBlock ||
               block instanceof CocoaBlock ||
               block == Blocks.NETHER_WART ||
               block instanceof SweetBerryBushBlock ||
               block instanceof BambooBlock ||
               block instanceof KelpBlock ||
               block instanceof TwistingVinesBlock ||
               block instanceof WeepingVinesBlock;
    }
    
    private static boolean growBlock(ServerWorld world, BlockPos pos, BlockState state, Block block) {
        if (block instanceof SaplingBlock sapling) {
            sapling.generate(world, pos, state, world.random);
            return world.getBlockState(pos).getBlock() != block;
        }
        
        if (block instanceof CropBlock crop) {
            int maxAge = crop.getMaxAge();
            int currentAge = crop.getAge(state);
            if (currentAge < maxAge) {
                world.setBlockState(pos, crop.withAge(maxAge));
                return true;
            }
        }
        
        if (block instanceof StemBlock stem) {
            int currentAge = state.get(StemBlock.AGE);
            if (currentAge < 7) {
                world.setBlockState(pos, state.with(StemBlock.AGE, 7));
                return true;
            }
        }
        
        if (block instanceof CocoaBlock) {
            int currentAge = state.get(CocoaBlock.AGE);
            if (currentAge < 2) {
                world.setBlockState(pos, state.with(CocoaBlock.AGE, 2));
                return true;
            }
        }
        
        if (block == Blocks.NETHER_WART) {
            int currentAge = state.get(NetherWartBlock.AGE);
            if (currentAge < 3) {
                world.setBlockState(pos, state.with(NetherWartBlock.AGE, 3));
                return true;
            }
        }
        
        if (block instanceof SweetBerryBushBlock) {
            int currentAge = state.get(SweetBerryBushBlock.AGE);
            if (currentAge < 3) {
                world.setBlockState(pos, state.with(SweetBerryBushBlock.AGE, 3));
                return true;
            }
        }
        
        return false;
    }
}
