package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.Arrays;

public class BeastMasterEvent extends CustomEventsConfigFields {
    public BeastMasterEvent(){
        super("beast_master", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(Arrays.asList("beast_master.yml"));
        setLocalCooldown(120D);
        setGlobalCooldown(25D);
        setWeight(80);
        setCustomSpawn("normal_surface_spawn.yml");
        setEventDuration(30);
    }
}
