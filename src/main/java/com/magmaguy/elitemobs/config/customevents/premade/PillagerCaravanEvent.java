package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.List;

public class PillagerCaravanEvent extends CustomEventsConfigFields {
    public PillagerCaravanEvent() {
        super("pillager_caravan", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(List.of("pillager_caravan_leader.yml"));
        setLocalCooldown(120D);
        setGlobalCooldown(25D);
        setWeight(80D);
        setSpawnType("normal_surface_spawn.yml");
        setEventDuration(30);
    }
}
