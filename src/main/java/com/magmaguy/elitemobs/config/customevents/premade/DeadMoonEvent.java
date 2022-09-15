package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.List;

public class DeadMoonEvent extends CustomEventsConfigFields {
    public DeadMoonEvent() {
        super("dead_moon", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(List.of("zombie_king.yml"));
        setLocalCooldown(240D);
        setGlobalCooldown(25D);
        setWeight(50D);
        setCustomSpawn("dead_moon_spawn.yml");
        setEventDuration(10D);
        setEventEndTime(23000);
    }
}
