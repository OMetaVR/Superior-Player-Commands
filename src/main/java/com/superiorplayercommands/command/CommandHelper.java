package com.superiorplayercommands.command;

import com.superiorplayercommands.config.ModConfig;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Helper class for command execution checks.
 */
public class CommandHelper {
    
    /**
     * Checks if a command is enabled and sends feedback if disabled.
     * @param source The command source
     * @param commandName The name of the command (without /)
     * @return true if the command is enabled and can execute
     */
    public static boolean isEnabled(ServerCommandSource source, String commandName) {
        if (!ModConfig.isCommandEnabled(commandName)) {
            source.sendFeedback(() -> Text.literal("§7[§cSPC§7] §cThis command is disabled. Use /spcsettings to enable it."), false);
            return false;
        }
        return true;
    }
    
    /**
     * Checks if the master toggle is enabled.
     * @return true if the mod is enabled
     */
    public static boolean isMasterEnabled() {
        return ModConfig.isMasterEnabled();
    }
}
