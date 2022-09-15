package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.List;

public class ZombieTotemEvent extends CustomEventsConfigFields {
    public ZombieTotemEvent() {
        super("zombie_totem", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(List.of("totem_zombie_1.yml"));
        setLocalCooldown(120D);
        setGlobalCooldown(25D);
        setWeight(100D);
        setCustomSpawn("normal_surface_spawn.yml");
        setEventDuration(30);
    }

}
