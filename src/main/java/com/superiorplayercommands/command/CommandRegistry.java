package com.superiorplayercommands.command;

import com.superiorplayercommands.SuperiorPlayerCommands;
import com.superiorplayercommands.command.core.*;
import com.superiorplayercommands.command.inventory.*;
import com.superiorplayercommands.command.power.*;
import com.superiorplayercommands.command.util.*;
import com.superiorplayercommands.command.world.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandRegistry {
    
    public static void registerAll() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SuperiorPlayerCommands.LOGGER.info("Registering Superior Player Commands...");
            
            // Core Commands
            JumpCommand.register(dispatcher);
            AscendCommand.register(dispatcher);
            DescendCommand.register(dispatcher);
            BackCommand.register(dispatcher);
            UnstuckCommand.register(dispatcher);
            BindCommand.register(dispatcher);
            AliasCommand.register(dispatcher);
            
            // Inventory Commands
            SetCommand.register(dispatcher);
            RemCommand.register(dispatcher);
            GotoCommand.register(dispatcher);
            
            // Player/Inventory Commands
            HealCommand.register(dispatcher);
            HungerCommand.register(dispatcher);
            ReplenishCommand.register(dispatcher);
            RepairCommand.register(dispatcher);
            DestroyCommand.register(dispatcher);
            DuplicateCommand.register(dispatcher);
            MoreCommand.register(dispatcher);
            StackCommand.register(dispatcher);
            
            // Power Commands
            GodCommand.register(dispatcher);
            FlyCommand.register(dispatcher);
            NoclipCommand.register(dispatcher);
            MobsIgnoreCommand.register(dispatcher);
            InstamineCommand.register(dispatcher);
            DropsCommand.register(dispatcher);
            HandsCommand.register(dispatcher);
            AutosmeltCommand.register(dispatcher);
            WaterwalkCommand.register(dispatcher);
            KnockbackCommand.register(dispatcher);
            FullbrightCommand.register(dispatcher);
            
            // World Commands
            ExtinguishCommand.register(dispatcher);
            FreezeCommand.register(dispatcher);
            KillallCommand.register(dispatcher);
            
            // Util Commands
            CalcCommand.register(dispatcher);
            BiomeCommand.register(dispatcher);
            MeasureCommand.register(dispatcher);
            TpsCommand.register(dispatcher);
            
            SuperiorPlayerCommands.LOGGER.info("Superior Player Commands registered!");
        });
    }
}

