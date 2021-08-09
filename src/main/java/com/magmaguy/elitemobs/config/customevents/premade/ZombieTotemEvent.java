package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.Arrays;

public class ZombieTotemEvent extends CustomEventsConfigFields {
    public ZombieTotemEvent(){
        super("zombie_totem", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(Arrays.asList("totem_zombie_1.yml"));
        setLocalCooldown(60D);
        setGlobalCooldown(15D);
        setWeight(120D);
        setCustomSpawn("normal_surface_spawn.yml");
        setEventDuration(60);
    }

}
