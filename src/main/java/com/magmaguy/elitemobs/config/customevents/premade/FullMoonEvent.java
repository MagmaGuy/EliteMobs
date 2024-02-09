package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.List;

public class FullMoonEvent extends CustomEventsConfigFields {
    public FullMoonEvent() {
        super("full_moon", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(List.of("alpha_werewolf_p1.yml"));
        setLocalCooldown(240D);
        setGlobalCooldown(25D);
        setWeight(50D);
        setSpawnType("full_moon_spawn.yml");
        setEventDuration(10D);
        setEventEndTime(23000);
    }
}
