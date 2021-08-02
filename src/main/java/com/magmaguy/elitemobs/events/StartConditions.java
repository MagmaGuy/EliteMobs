package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import org.bukkit.Bukkit;

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
