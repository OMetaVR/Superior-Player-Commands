package com.superiorplayercommands.command;

import com.superiorplayercommands.SuperiorPlayerCommands;
import com.superiorplayercommands.command.core.*;
import com.superiorplayercommands.command.inventory.*;
import com.superiorplayercommands.command.power.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandRegistry {
    
    public static void registerAll() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SuperiorPlayerCommands.LOGGER.info("Registering Superior Player Commands...");
            
            // Core utilities
            JumpCommand.register(dispatcher);
            AscendCommand.register(dispatcher);
            DescendCommand.register(dispatcher);
            BindCommand.register(dispatcher);
            
            // Waypoints
            SetCommand.register(dispatcher);
            RemCommand.register(dispatcher);
            GotoCommand.register(dispatcher);
            
            // Inventory/Player commands
            HealCommand.register(dispatcher);
            ReplenishCommand.register(dispatcher);
            RepairCommand.register(dispatcher);
            DestroyCommand.register(dispatcher);
            DuplicateCommand.register(dispatcher);
            
            // Power tools
            InstamineCommand.register(dispatcher);
            DropsCommand.register(dispatcher);
            HandsCommand.register(dispatcher);
            
            SuperiorPlayerCommands.LOGGER.info("Superior Player Commands registered!");
        });
    }
}

