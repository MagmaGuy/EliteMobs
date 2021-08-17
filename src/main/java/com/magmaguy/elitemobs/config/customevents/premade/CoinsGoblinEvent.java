package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.Arrays;

public class CoinsGoblinEvent extends CustomEventsConfigFields {
    public CoinsGoblinEvent(){
        super("coins_goblin", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(Arrays.asList("coins_goblin.yml"));
        setLocalCooldown(120D);
        setGlobalCooldown(25D);
        setWeight(75);
        setCustomSpawn("normal_surface_spawn.yml");
        setEventDuration(30);
    }
}
