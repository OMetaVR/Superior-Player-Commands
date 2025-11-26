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
            
            // Settings command (always available)
            SettingsCommand.register(dispatcher);
            
            // Core commands
            JumpCommand.register(dispatcher);
            AscendCommand.register(dispatcher);
            DescendCommand.register(dispatcher);
            BackCommand.register(dispatcher);
            ReturnCommand.register(dispatcher);
            UnstuckCommand.register(dispatcher);
            BindCommand.register(dispatcher);
            AliasCommand.register(dispatcher);
            HideResponsesCommand.register(dispatcher);
            UsePortalCommand.register(dispatcher);
            HelpCommand.register(dispatcher);
            
            SetCommand.register(dispatcher);
            RemCommand.register(dispatcher);
            GotoCommand.register(dispatcher);
            ListWaypointsCommand.register(dispatcher);
            
            HealCommand.register(dispatcher);
            HungerCommand.register(dispatcher);
            ReplenishCommand.register(dispatcher);
            RepairCommand.register(dispatcher);
            DestroyCommand.register(dispatcher);
            DuplicateCommand.register(dispatcher);
            MoreCommand.register(dispatcher);
            StackCommand.register(dispatcher);
            EnchCommand.register(dispatcher);
            DropCommand.register(dispatcher);
            DropStoreCommand.register(dispatcher);
            
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
            SetJumpCommand.register(dispatcher);
            SetSpeedCommand.register(dispatcher);
            FallDamageCommand.register(dispatcher);
            FireDamageCommand.register(dispatcher);
            DrownDamageCommand.register(dispatcher);
            HealthCommand.register(dispatcher);
            RideCommand.register(dispatcher);
            
            ExtinguishCommand.register(dispatcher);
            FreezeCommand.register(dispatcher);
            FreezeAICommand.register(dispatcher);
            KillallCommand.register(dispatcher);
            ExplodeCommand.register(dispatcher);
            LightningCommand.register(dispatcher);
            DefuseCommand.register(dispatcher);
            GrowCommand.register(dispatcher);
            SpawnStackCommand.register(dispatcher);
            
            CalcCommand.register(dispatcher);
            BiomeCommand.register(dispatcher);
            MeasureCommand.register(dispatcher);
            TpsCommand.register(dispatcher);
            CoordsCommand.register(dispatcher);
            MusicCommand.register(dispatcher);
            
            SuperiorPlayerCommands.LOGGER.info("Superior Player Commands registered!");
        });
    }
}

