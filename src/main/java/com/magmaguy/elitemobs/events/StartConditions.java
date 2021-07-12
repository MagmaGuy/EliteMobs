package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;

public class StartConditions {

    public int minimumPlayerCount;

    public StartConditions(CustomEventsConfigFields customEventsConfigFields) {
        this.minimumPlayerCount = customEventsConfigFields.getMinimumPlayerCount();
    }

    public boolean areValid(){
        if (Bukkit.getServer().getOnlinePlayers().size() < minimumPlayerCount) return false;
        return true;
    }

}
