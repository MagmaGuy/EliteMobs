package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.List;

public class CharmsGoblinEvent extends CustomEventsConfigFields {
    public CharmsGoblinEvent() {
        super("charms_goblin",
                true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(List.of("charms_goblin.yml"));
        setLocalCooldown(120D);
        setGlobalCooldown(25D);
        setWeight(50);
        setSpawnType("normal_surface_spawn.yml");
        setEventDuration(30);
    }
}
