package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.List;

public class TreasureGoblinEvent extends CustomEventsConfigFields {
    public TreasureGoblinEvent() {
        super("treasure_goblin", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(List.of("treasure_goblin.yml"));
        setLocalCooldown(120D);
        setGlobalCooldown(25D);
        setWeight(100D);
        setSpawnType("normal_surface_spawn.yml");
        setEventDuration(30);
    }
}
