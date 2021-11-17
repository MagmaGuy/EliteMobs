package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import org.bukkit.Bukkit;

public class StartConditions {

    private final int minimumPlayerCount;

    public StartConditions(CustomEventsConfigFields customEventsConfigFields) {
        this.minimumPlayerCount = customEventsConfigFields.getMinimumPlayerCount();
    }

    public boolean areValid() {
        return Bukkit.getServer().getOnlinePlayers().size() >= minimumPlayerCount;
    }

}
