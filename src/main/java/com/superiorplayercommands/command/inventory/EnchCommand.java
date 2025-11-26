package com.superiorplayercommands.command.inventory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.superiorplayercommands.data.PlayerStateManager;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class EnchCommand {
    
    private static final SuggestionProvider<ServerCommandSource> ENCHANTMENT_SUGGESTIONS = (context, builder) -> {
        List<String> names = new ArrayList<>();
        for (Enchantment ench : Registries.ENCHANTMENT) {
            Identifier id = Registries.ENCHANTMENT.getId(ench);
            if (id != null) {
                names.add(id.getPath());
            }
        }
        return CommandSource.suggestMatching(names, builder);
    };
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("ench")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("enchantment", StringArgumentType.word())
                    .suggests(ENCHANTMENT_SUGGESTIONS)
                    .executes(context -> execute(context, 1))
                    .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 255))
                        .executes(context -> execute(context, IntegerArgumentType.getInteger(context, "level")))
                    )
                )
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context, int level) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        ItemStack heldItem = player.getMainHandStack();
        
        if (heldItem.isEmpty()) {
            source.sendFeedback(() -> Text.literal("You're not holding anything")
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        String enchantName = StringArgumentType.getString(context, "enchantment").toLowerCase();
        
        Identifier enchId = new Identifier("minecraft", enchantName);
        Enchantment enchantment = Registries.ENCHANTMENT.get(enchId);
        
        if (enchantment == null) {
            source.sendFeedback(() -> Text.literal("Unknown enchantment: " + enchantName)
                .formatted(Formatting.RED), false);
            return 0;
        }
        
        if (level == 0) {
            removeEnchantment(heldItem, enchantment);
            if (!PlayerStateManager.isHideResponses(player.getUuid())) {
                source.sendFeedback(() -> Text.literal("Removed ")
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal(formatEnchantName(enchantName))
                        .formatted(Formatting.AQUA)), false);
            }
            return 1;
        }
        
        addEnchantmentUnsafe(heldItem, enchantment, level);
        
        if (!PlayerStateManager.isHideResponses(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("Applied ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(formatEnchantName(enchantName) + " " + level)
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" to held item")
                    .formatted(Formatting.GREEN)), false);
        }
        
        return 1;
    }
    
    private static void addEnchantmentUnsafe(ItemStack stack, Enchantment enchantment, int level) {
        Identifier id = Registries.ENCHANTMENT.getId(enchantment);
        if (id == null) return;
        
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList enchantments;
        
        String enchKey = stack.getItem().equals(net.minecraft.item.Items.ENCHANTED_BOOK) 
            ? "StoredEnchantments" 
            : "Enchantments";
        
        if (nbt.contains(enchKey, 9)) {
            enchantments = nbt.getList(enchKey, 10);

            for (int i = enchantments.size() - 1; i >= 0; i--) {
                NbtCompound enchNbt = enchantments.getCompound(i);
                if (enchNbt.getString("id").equals(id.toString())) {
                    enchantments.remove(i);
                }
            }
        } else {
            enchantments = new NbtList();
        }
        
        NbtCompound enchNbt = new NbtCompound();
        enchNbt.putString("id", id.toString());
        enchNbt.putShort("lvl", (short) level);
        enchantments.add(enchNbt);
        
        nbt.put(enchKey, enchantments);
    }
    
    private static void removeEnchantment(ItemStack stack, Enchantment enchantment) {
        Identifier id = Registries.ENCHANTMENT.getId(enchantment);
        if (id == null) return;
        
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return;
        
        String enchKey = stack.getItem().equals(net.minecraft.item.Items.ENCHANTED_BOOK) 
            ? "StoredEnchantments" 
            : "Enchantments";
        
        if (nbt.contains(enchKey, 9)) {
            NbtList enchantments = nbt.getList(enchKey, 10);
            for (int i = enchantments.size() - 1; i >= 0; i--) {
                NbtCompound enchNbt = enchantments.getCompound(i);
                if (enchNbt.getString("id").equals(id.toString())) {
                    enchantments.remove(i);
                }
            }
        }
    }
    
    private static String formatEnchantName(String name) {
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(" ");
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) result.append(word.substring(1));
        }
        return result.toString();
    }
}
