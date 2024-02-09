package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.List;

public class CoinsGoblinEvent extends CustomEventsConfigFields {
    public CoinsGoblinEvent() {
        super("coins_goblin", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(List.of("coins_goblin.yml"));
        setLocalCooldown(120D);
        setGlobalCooldown(25D);
        setWeight(75);
        setSpawnType("normal_surface_spawn.yml");
        setEventDuration(30);
    }
}
