package com.superiorplayercommands.command.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.*;

public class KillallCommand {
    
    private static final int DEFAULT_RADIUS = 128;
    private static final int MAX_RADIUS = 256;
    
    private static final Map<String, Set<EntityType<?>>> GROUPS = new HashMap<>();
    
    static {
        GROUPS.put("hostiles", Set.of(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
            EntityType.CAVE_SPIDER, EntityType.ENDERMAN, EntityType.WITCH, EntityType.SLIME,
            EntityType.MAGMA_CUBE, EntityType.BLAZE, EntityType.GHAST, EntityType.WITHER_SKELETON,
            EntityType.STRAY, EntityType.HUSK, EntityType.DROWNED, EntityType.PHANTOM,
            EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER, EntityType.RAVAGER,
            EntityType.VEX, EntityType.HOGLIN, EntityType.PIGLIN_BRUTE, EntityType.ZOGLIN,
            EntityType.WARDEN, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.SHULKER,
            EntityType.SILVERFISH, EntityType.ENDERMITE
        ));
        
        GROUPS.put("passives", Set.of(
            EntityType.PIG, EntityType.COW, EntityType.SHEEP, EntityType.CHICKEN,
            EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.RABBIT,
            EntityType.LLAMA, EntityType.PARROT, EntityType.CAT, EntityType.WOLF,
            EntityType.FOX, EntityType.PANDA, EntityType.POLAR_BEAR, EntityType.TURTLE,
            EntityType.DOLPHIN, EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.COD,
            EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH,
            EntityType.AXOLOTL, EntityType.GOAT, EntityType.FROG, EntityType.ALLAY,
            EntityType.CAMEL, EntityType.SNIFFER, EntityType.VILLAGER, EntityType.WANDERING_TRADER,
            EntityType.BEE, EntityType.STRIDER, EntityType.MOOSHROOM, EntityType.OCELOT
        ));
        
        GROUPS.put("zombies", Set.of(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN
        ));
        
        GROUPS.put("skeletons", Set.of(
            EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON
        ));
        
        GROUPS.put("undead", Set.of(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED,
            EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.PHANTOM,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN, EntityType.WITHER
        ));
        
        GROUPS.put("items", Set.of(EntityType.ITEM));
        
        GROUPS.put("xp", Set.of(EntityType.EXPERIENCE_ORB));
        
        GROUPS.put("projectiles", Set.of(
            EntityType.ARROW, EntityType.SPECTRAL_ARROW, EntityType.TRIDENT,
            EntityType.FIREBALL, EntityType.SMALL_FIREBALL, EntityType.DRAGON_FIREBALL,
            EntityType.WITHER_SKULL, EntityType.SHULKER_BULLET, EntityType.LLAMA_SPIT,
            EntityType.EGG, EntityType.SNOWBALL, EntityType.ENDER_PEARL, EntityType.EYE_OF_ENDER,
            EntityType.POTION, EntityType.EXPERIENCE_BOTTLE, EntityType.FIREWORK_ROCKET
        ));
    }
    
    private static final SuggestionProvider<ServerCommandSource> TYPE_SUGGESTIONS = (context, builder) -> {
        List<String> suggestions = new ArrayList<>(GROUPS.keySet());
        suggestions.add("all");
        suggestions.addAll(List.of("zombie", "skeleton", "creeper", "spider", "enderman", 
            "pig", "cow", "sheep", "chicken", "villager"));
        return CommandSource.suggestMatching(suggestions, builder);
    };
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("killall")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> execute(context, "all", DEFAULT_RADIUS))
                .then(CommandManager.argument("type", StringArgumentType.word())
                    .suggests(TYPE_SUGGESTIONS)
                    .executes(context -> execute(context, StringArgumentType.getString(context, "type"), DEFAULT_RADIUS))
                    .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                        .executes(context -> execute(context, 
                            StringArgumentType.getString(context, "type"),
                            IntegerArgumentType.getInteger(context, "radius")))
                    )
                )
        );
        
        dispatcher.register(
            CommandManager.literal("butcher")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> execute(context, "hostiles", DEFAULT_RADIUS))
                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                    .executes(context -> execute(context, "hostiles", 
                        IntegerArgumentType.getInteger(context, "radius")))
                )
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context, String type, int radius) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        Box area = player.getBoundingBox().expand(radius);
        
        List<Entity> toKill = new ArrayList<>();
        String typeLower = type.toLowerCase();
        
        if (GROUPS.containsKey(typeLower)) {
            Set<EntityType<?>> groupTypes = GROUPS.get(typeLower);
            toKill = player.getWorld().getEntitiesByClass(Entity.class, area, 
                e -> groupTypes.contains(e.getType()) && !(e instanceof PlayerEntity));
        } else if (typeLower.equals("all")) {
            toKill = player.getWorld().getEntitiesByClass(Entity.class, area,
                e -> !(e instanceof PlayerEntity));
        } else {
            Identifier entityId = new Identifier("minecraft", typeLower);
            Optional<EntityType<?>> entityType = Registries.ENTITY_TYPE.getOrEmpty(entityId);
            
            if (entityType.isPresent()) {
                EntityType<?> et = entityType.get();
                toKill = player.getWorld().getEntitiesByClass(Entity.class, area,
                    e -> e.getType() == et && !(e instanceof PlayerEntity));
            } else {
                source.sendFeedback(() -> Text.literal("Unknown type: " + type)
                    .formatted(Formatting.RED)
                    .append(Text.literal("\nGroups: hostiles, passives, zombies, skeletons, undead, items, xp, projectiles, all")
                        .formatted(Formatting.GRAY)), false);
                return 0;
            }
        }
        
        int killed = 0;
        for (Entity entity : toKill) {
            entity.kill();
            killed++;
        }
        
        final int count = killed;
        final String finalType = type;
        
        if (count > 0) {
            source.sendFeedback(() -> Text.literal("Killed ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(count))
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" " + finalType + (count != 1 && !finalType.endsWith("s") ? "s" : ""))
                    .formatted(Formatting.GREEN)), false);
        } else {
            source.sendFeedback(() -> Text.literal("No " + finalType + " found within " + radius + " blocks")
                .formatted(Formatting.GRAY), false);
        }
        
        return count;
    }
}
