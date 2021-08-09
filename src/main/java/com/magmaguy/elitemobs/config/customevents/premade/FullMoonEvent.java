package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.Arrays;

public class FullMoonEvent extends CustomEventsConfigFields {
    public FullMoonEvent() {
        super("full_moon", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(Arrays.asList("alpha_werewolf_p1.yml"));
        setLocalCooldown(60D);
        setGlobalCooldown(15D);
        setWeight(50D);
        setCustomSpawn("full_moon_spawn.yml");
        setEventDuration(60D);
    }
}
