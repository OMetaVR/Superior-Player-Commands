package com.superiorplayercommands.command.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class CalcCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("calc")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.argument("expression", StringArgumentType.greedyString())
                    .executes(CalcCommand::execute)
                )
        );
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String expression = StringArgumentType.getString(context, "expression");
        
        try {
            double result = evaluate(expression);
            String resultStr;
            
            if (result == Math.floor(result) && !Double.isInfinite(result)) {
                resultStr = String.format("%.0f", result);
            } else {
                resultStr = String.format("%.6f", result).replaceAll("0+$", "").replaceAll("\\.$", "");
            }
            
            source.sendFeedback(() -> Text.literal(expression + " = ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(resultStr)
                    .formatted(Formatting.AQUA)), false);
            
            return 1;
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("Invalid expression: " + e.getMessage())
                .formatted(Formatting.RED), false);
            return 0;
        }
    }
    
    private static double evaluate(String expression) throws Exception {
        expression = expression.replaceAll("\\s+", "");
        
        expression = expression.replace("sqrt", "Math.sqrt");
        expression = expression.replace("pow", "Math.pow");
        expression = expression.replace("abs", "Math.abs");
        expression = expression.replace("sin", "Math.sin");
        expression = expression.replace("cos", "Math.cos");
        expression = expression.replace("tan", "Math.tan");
        expression = expression.replace("log", "Math.log");
        expression = expression.replace("pi", String.valueOf(Math.PI));
        expression = expression.replace("e", String.valueOf(Math.E));
        
        return parseExpression(expression, new int[]{0});
    }
    
    private static double parseExpression(String expr, int[] pos) throws Exception {
        double result = parseTerm(expr, pos);
        
        while (pos[0] < expr.length()) {
            char op = expr.charAt(pos[0]);
            if (op == '+') {
                pos[0]++;
                result += parseTerm(expr, pos);
            } else if (op == '-') {
                pos[0]++;
                result -= parseTerm(expr, pos);
            } else {
                break;
            }
        }
        
        return result;
    }
    
    private static double parseTerm(String expr, int[] pos) throws Exception {
        double result = parseFactor(expr, pos);
        
        while (pos[0] < expr.length()) {
            char op = expr.charAt(pos[0]);
            if (op == '*') {
                pos[0]++;
                result *= parseFactor(expr, pos);
            } else if (op == '/') {
                pos[0]++;
                result /= parseFactor(expr, pos);
            } else if (op == '%') {
                pos[0]++;
                result %= parseFactor(expr, pos);
            } else if (op == '^') {
                pos[0]++;
                result = Math.pow(result, parseFactor(expr, pos));
            } else {
                break;
            }
        }
        
        return result;
    }
    
    private static double parseFactor(String expr, int[] pos) throws Exception {
        if (pos[0] >= expr.length()) {
            throw new Exception("Unexpected end of expression");
        }
        
        boolean negative = false;
        if (expr.charAt(pos[0]) == '-') {
            negative = true;
            pos[0]++;
        }
        
        double result;
        
        if (expr.charAt(pos[0]) == '(') {
            pos[0]++;
            result = parseExpression(expr, pos);
            if (pos[0] >= expr.length() || expr.charAt(pos[0]) != ')') {
                throw new Exception("Missing closing parenthesis");
            }
            pos[0]++;
        } else {
            int start = pos[0];
            while (pos[0] < expr.length() && 
                   (Character.isDigit(expr.charAt(pos[0])) || expr.charAt(pos[0]) == '.')) {
                pos[0]++;
            }
            if (start == pos[0]) {
                throw new Exception("Expected number at position " + pos[0]);
            }
            result = Double.parseDouble(expr.substring(start, pos[0]));
        }
        
        return negative ? -result : result;
    }
}
