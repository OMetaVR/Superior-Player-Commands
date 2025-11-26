package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HelpCommand {
    
    private static final Map<String, String> COMMANDS = new LinkedHashMap<>();
    
    static {
        COMMANDS.put("jump", "Teleport to where you're looking");
        COMMANDS.put("ascend", "Teleport up through blocks");
        COMMANDS.put("descend", "Teleport down through blocks");
        COMMANDS.put("back", "Return to death location");
        COMMANDS.put("return", "Return to last teleport location");
        COMMANDS.put("unstuck", "Free yourself from blocks");
        COMMANDS.put("useportal", "Instantly use nearest portal");
        COMMANDS.put("set <name>", "Create a waypoint");
        COMMANDS.put("rem <name>", "Remove a waypoint");
        COMMANDS.put("goto <name>", "Teleport to waypoint");
        COMMANDS.put("listwaypoints", "List all waypoints (alias: /l)");
        COMMANDS.put("bind <key> <command>", "Bind command to key");
        COMMANDS.put("alias <name> <command>", "Create command alias");
        COMMANDS.put("hideresponses", "Toggle command feedback");
        
        COMMANDS.put("god", "Toggle invincibility");
        COMMANDS.put("fly", "Toggle flight");
        COMMANDS.put("noclip", "Toggle noclip mode");
        COMMANDS.put("mobsignore", "Toggle mob targeting");
        COMMANDS.put("instamine", "Toggle instant mining");
        COMMANDS.put("autosmelt", "Toggle auto-smelting");
        COMMANDS.put("waterwalk", "Toggle walking on water");
        COMMANDS.put("fullbright", "Toggle fullbright");
        COMMANDS.put("knockback <mult>", "Set knockback multiplier");
        COMMANDS.put("drops", "Toggle block drops");
        COMMANDS.put("hands <tier>", "Set bare-hand mining tier");
        COMMANDS.put("setjump <value|reset>", "Set jump multiplier");
        COMMANDS.put("setspeed <value|reset>", "Set speed multiplier");
        COMMANDS.put("falldamage", "Toggle fall damage");
        COMMANDS.put("firedamage", "Toggle fire damage");
        COMMANDS.put("drowndamage", "Toggle drown damage");
        COMMANDS.put("health <min|max|infinite>", "Set health mode");
        COMMANDS.put("ride", "Ride entity you're looking at");
        
        COMMANDS.put("heal", "Restore health and hunger");
        COMMANDS.put("hunger <value>", "Set hunger level");
        COMMANDS.put("saturation <value>", "Set saturation");
        COMMANDS.put("repair", "Repair held item");
        COMMANDS.put("destroy", "Destroy held item");
        COMMANDS.put("duplicate [all] [store]", "Duplicate items");
        COMMANDS.put("more [all]", "Fill stack to max");
        COMMANDS.put("stack", "Combine similar items");
        COMMANDS.put("ench <enchant> [level]", "Enchant held item");
        COMMANDS.put("drop", "Drop all items");
        COMMANDS.put("dropstore", "Store inventory in chest");
        
        COMMANDS.put("explode [power]", "Create explosion");
        COMMANDS.put("lightning", "Strike lightning");
        COMMANDS.put("extinguish [radius]", "Put out fires");
        COMMANDS.put("freeze", "Toggle mob AI freeze");
        COMMANDS.put("killall [type] [radius]", "Kill entities");
        COMMANDS.put("defuse [radius]", "Remove explosives");
        COMMANDS.put("grow [radius] [type]", "Grow plants");
        COMMANDS.put("spawnstack <mobs...> <count>", "Spawn stacked mobs");
        
        COMMANDS.put("calc <expression>", "Calculate math");
        COMMANDS.put("biome", "Show current biome");
        COMMANDS.put("measure", "Measure distances");
        COMMANDS.put("tps", "Show server TPS");
        COMMANDS.put("coords", "Show coordinates");
        COMMANDS.put("music <play|pause|skip|back>", "Control music");
        COMMANDS.put("help [command]", "Show this help");
    }
    
    private static final SuggestionProvider<ServerCommandSource> COMMAND_SUGGESTIONS = (context, builder) -> {
        return CommandSource.suggestMatching(COMMANDS.keySet().stream()
            .map(s -> s.split(" ")[0]), builder);
    };
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("help")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(HelpCommand::executeList)
                .then(CommandManager.argument("command", StringArgumentType.word())
                    .suggests(COMMAND_SUGGESTIONS)
                    .executes(HelpCommand::executeSpecific))
        );
        
        dispatcher.register(
            CommandManager.literal("h")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(HelpCommand::executeList)
                .then(CommandManager.argument("command", StringArgumentType.word())
                    .suggests(COMMAND_SUGGESTIONS)
                    .executes(HelpCommand::executeSpecific))
        );
    }
    
    private static int executeList(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        source.sendFeedback(() -> Text.literal("=== Superior Player Commands ===")
            .formatted(Formatting.GOLD), false);
        
        source.sendFeedback(() -> Text.literal("Use ")
            .formatted(Formatting.GRAY)
            .append(Text.literal("/help <command>")
                .formatted(Formatting.YELLOW))
            .append(Text.literal(" for details")
                .formatted(Formatting.GRAY)), false);
        
        source.sendFeedback(() -> Text.literal(""), false);
        
        source.sendFeedback(() -> Text.literal("Core: ")
            .formatted(Formatting.AQUA)
            .append(Text.literal("jump, ascend, descend, back, return, set, rem, goto, listwaypoints, bind, alias")
                .formatted(Formatting.WHITE)), false);
        
        source.sendFeedback(() -> Text.literal("Power: ")
            .formatted(Formatting.AQUA)
            .append(Text.literal("god, fly, noclip, mobsignore, instamine, autosmelt, waterwalk, fullbright, knockback, setjump, setspeed, falldamage, firedamage, drowndamage, health, ride")
                .formatted(Formatting.WHITE)), false);
        
        source.sendFeedback(() -> Text.literal("Inventory: ")
            .formatted(Formatting.AQUA)
            .append(Text.literal("heal, hunger, saturation, repair, destroy, duplicate, more, stack, ench, drop, dropstore")
                .formatted(Formatting.WHITE)), false);
        
        source.sendFeedback(() -> Text.literal("World: ")
            .formatted(Formatting.AQUA)
            .append(Text.literal("explode, lightning, extinguish, freeze, killall, defuse, grow, spawnstack")
                .formatted(Formatting.WHITE)), false);
        
        source.sendFeedback(() -> Text.literal("Util: ")
            .formatted(Formatting.AQUA)
            .append(Text.literal("calc, biome, measure, tps, coords, music, help")
                .formatted(Formatting.WHITE)), false);
        
        return 1;
    }
    
    private static int executeSpecific(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String cmd = StringArgumentType.getString(context, "command").toLowerCase();
        
        for (Map.Entry<String, String> entry : COMMANDS.entrySet()) {
            if (entry.getKey().startsWith(cmd + " ") || entry.getKey().equals(cmd)) {
                source.sendFeedback(() -> Text.literal("/" + entry.getKey())
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal(" - " + entry.getValue())
                        .formatted(Formatting.WHITE)), false);
                return 1;
            }
        }
        
        source.sendFeedback(() -> Text.literal("Unknown command: " + cmd)
            .formatted(Formatting.RED), false);
        return 0;
    }
}
