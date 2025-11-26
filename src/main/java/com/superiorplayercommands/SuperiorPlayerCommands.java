package com.superiorplayercommands;

import com.superiorplayercommands.alias.AliasManager;
import com.superiorplayercommands.command.CommandRegistry;
import com.superiorplayercommands.data.WaypointManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperiorPlayerCommands implements ModInitializer {
    public static final String MOD_ID = "superior-player-commands";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Superior Player Commands initializing...");
        
        WaypointManager.load();
        AliasManager.load();
        
        CommandRegistry.registerAll();
        
        LOGGER.info("Superior Player Commands initialized!");
    }
}

