package com.superiorplayercommands.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.superiorplayercommands.bind.BindManager;
import com.superiorplayercommands.bind.KeyHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class BindCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("bind")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(BindCommand::listBindings)
                .then(CommandManager.argument("key", StringArgumentType.word())
                    .executes(BindCommand::showOrUnbind)
                    .then(CommandManager.argument("command", StringArgumentType.greedyString())
                        .executes(BindCommand::setBind)
                    )
                )
        );
        
        dispatcher.register(
            CommandManager.literal("unbind")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.argument("key", StringArgumentType.word())
                    .executes(BindCommand::unbind)
                )
        );
    }
    
    private static int listBindings(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Map<String, String> bindings = BindManager.getAllBindings();
        
        if (bindings.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No key bindings set. Use ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("/bind <key> <command>")
                    .formatted(Formatting.YELLOW))
                .append(Text.literal(" to create one.")), false);
            return 0;
        }
        
        source.sendFeedback(() -> Text.literal("=== Key Bindings ===")
            .formatted(Formatting.GOLD), false);
        
        for (Map.Entry<String, String> entry : bindings.entrySet()) {
            String key = entry.getKey();
            String command = entry.getValue();
            
            MutableText line = Text.literal("[" + key.toUpperCase() + "] ")
                .formatted(Formatting.AQUA)
                .append(Text.literal("→ ")
                    .formatted(Formatting.GRAY))
                .append(Text.literal("/" + command)
                    .formatted(Formatting.WHITE));
            
            line.styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/unbind " + key))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    Text.literal("Click to unbind").formatted(Formatting.RED)))
            );
            
            source.sendFeedback(() -> line, false);
        }
        
        return bindings.size();
    }
    
    private static int showOrUnbind(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String key = StringArgumentType.getString(context, "key").toLowerCase();
        
        if (!KeyHelper.isValidKey(key)) {
            source.sendFeedback(() -> Text.literal("Unknown key: " + key)
                .formatted(Formatting.RED)
                .append(Text.literal("\nValid keys: a-z, 0-9, f1-f12, space, tab, etc.")
                    .formatted(Formatting.GRAY)), false);
            return 0;
        }
        
        var binding = BindManager.getBind(key);
        if (binding.isPresent()) {
            source.sendFeedback(() -> Text.literal("[" + key.toUpperCase() + "] ")
                .formatted(Formatting.AQUA)
                .append(Text.literal("is bound to: ")
                    .formatted(Formatting.GRAY))
                .append(Text.literal("/" + binding.get())
                    .formatted(Formatting.WHITE)), false);
        } else {
            source.sendFeedback(() -> Text.literal("[" + key.toUpperCase() + "] ")
                .formatted(Formatting.AQUA)
                .append(Text.literal("is not bound")
                    .formatted(Formatting.GRAY)), false);
        }
        
        return 1;
    }
    
    private static int setBind(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String key = StringArgumentType.getString(context, "key").toLowerCase();
        String command = StringArgumentType.getString(context, "command");
        
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        if (!KeyHelper.isValidKey(key)) {
            source.sendFeedback(() -> Text.literal("Unknown key: " + key)
                .formatted(Formatting.RED)
                .append(Text.literal("\nValid keys: a-z, 0-9, f1-f12, space, tab, etc.")
                    .formatted(Formatting.GRAY)), false);
            return 0;
        }
        
        final String finalCommand = command;
        BindManager.setBind(key, command);
        
        source.sendFeedback(() -> Text.literal("Bound [" + key.toUpperCase() + "] to: ")
            .formatted(Formatting.GREEN)
            .append(Text.literal("/" + finalCommand)
                .formatted(Formatting.WHITE)), false);
        
        return 1;
    }
    
    private static int unbind(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String key = StringArgumentType.getString(context, "key").toLowerCase();
        
        if (BindManager.removeBind(key)) {
            source.sendFeedback(() -> Text.literal("Unbound [" + key.toUpperCase() + "]")
                .formatted(Formatting.YELLOW), false);
            return 1;
        } else {
            source.sendFeedback(() -> Text.literal("[" + key.toUpperCase() + "] was not bound")
                .formatted(Formatting.GRAY), false);
            return 0;
        }
    }
}




