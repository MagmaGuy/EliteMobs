package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.List;

public class ArmorGoblinEvent extends CustomEventsConfigFields {
    public ArmorGoblinEvent() {
        super("armor_goblin", true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(List.of("armor_goblin.yml"));
        setLocalCooldown(120D);
        setGlobalCooldown(25D);
        setWeight(100D);
        setSpawnType("normal_surface_spawn.yml");
        setEventDuration(30);
    }
}
