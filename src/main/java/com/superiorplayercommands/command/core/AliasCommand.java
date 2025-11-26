package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.alias.AliasManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class AliasCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("alias")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(AliasCommand::listAliases)
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(AliasCommand::showAlias)
                    .then(CommandManager.argument("command", StringArgumentType.greedyString())
                        .executes(AliasCommand::setAlias)
                    )
                )
        );
        
        dispatcher.register(
            CommandManager.literal("unalias")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(AliasCommand::removeAlias)
                )
        );
        
        registerAliasCommands(dispatcher);
    }
    
    public static void registerAliasCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        for (Map.Entry<String, String> entry : AliasManager.getAllAliases().entrySet()) {
            registerSingleAlias(dispatcher, entry.getKey(), entry.getValue());
        }
    }
    
    private static void registerSingleAlias(CommandDispatcher<ServerCommandSource> dispatcher, String name, String command) {
        try {
            dispatcher.register(
                CommandManager.literal(name)
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> executeAlias(context, command))
            );
        } catch (Exception e) {

        }
    }
    
    private static int executeAlias(CommandContext<ServerCommandSource> context, String command) {
        ServerCommandSource source = context.getSource();
        
        try {
            return source.getServer().getCommandManager().executeWithPrefix(source, "/" + command);
        } catch (Exception e) {
            source.sendError(Text.literal("Failed to execute alias: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int listAliases(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Map<String, String> aliases = AliasManager.getAllAliases();
        
        if (aliases.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No aliases defined. Use ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("/alias <name> <command>")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal(" to create one.")), false);
            return 0;
        }
        
        source.sendFeedback(() -> Text.literal("=== Aliases ===")
            .formatted(Formatting.GOLD), false);
        
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            String name = entry.getKey();
            String command = entry.getValue();
            
            MutableText line = Text.literal("/" + name)
                .formatted(Formatting.AQUA)
                .append(Text.literal(" → ")
                    .formatted(Formatting.GRAY))
                .append(Text.literal("/" + command)
                    .formatted(Formatting.WHITE));
            
                line.styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/unalias " + name))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    Text.literal("Click to remove").formatted(Formatting.RED)))
            );
            
            source.sendFeedback(() -> line, false);
        }
        
        return aliases.size();
    }
    
    private static int showAlias(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String name = StringArgumentType.getString(context, "name").toLowerCase();
        
        var alias = AliasManager.getAlias(name);
        if (alias.isPresent()) {
            source.sendFeedback(() -> Text.literal("/" + name)
                .formatted(Formatting.AQUA)
                .append(Text.literal(" expands to: ")
                    .formatted(Formatting.GRAY))
                .append(Text.literal("/" + alias.get())
                    .formatted(Formatting.WHITE)), false);
        } else {
            source.sendFeedback(() -> Text.literal("No alias named '")
                .formatted(Formatting.RED)
                .append(Text.literal(name).formatted(Formatting.AQUA))
                .append(Text.literal("'").formatted(Formatting.RED)), false);
        }
        
        return 1;
    }
    
    private static int setAlias(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String name = StringArgumentType.getString(context, "name").toLowerCase();
        String command = StringArgumentType.getString(context, "command");
        
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        boolean isUpdate = AliasManager.hasAlias(name);
        AliasManager.setAlias(name, command);
        
        final String finalCommand = command;
        String action = isUpdate ? "Updated" : "Created";
        
        source.sendFeedback(() -> Text.literal(action + " alias ")
            .formatted(Formatting.GREEN)
            .append(Text.literal("/" + name)
                .formatted(Formatting.AQUA))
            .append(Text.literal(" → ")
                .formatted(Formatting.GRAY))
            .append(Text.literal("/" + finalCommand)
                .formatted(Formatting.WHITE)), false);
        
        if (!isUpdate) {
            source.sendFeedback(() -> Text.literal("Note: Restart or rejoin for the new alias to work as a command")
                .formatted(Formatting.YELLOW), false);
        }
        
        return 1;
    }
    
    private static int removeAlias(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String name = StringArgumentType.getString(context, "name").toLowerCase();
        
        if (AliasManager.removeAlias(name)) {
            source.sendFeedback(() -> Text.literal("Removed alias ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("/" + name)
                    .formatted(Formatting.AQUA)), false);
            return 1;
        } else {
            source.sendFeedback(() -> Text.literal("No alias named '")
                .formatted(Formatting.RED)
                .append(Text.literal(name).formatted(Formatting.AQUA))
                .append(Text.literal("'").formatted(Formatting.RED)), false);
            return 0;
        }
    }
}
