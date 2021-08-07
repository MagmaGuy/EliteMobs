package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.Arrays;

public class CharmsGoblinEvent extends CustomEventsConfigFields {
    public CharmsGoblinEvent(){
        super("charms_goblin",
                true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(Arrays.asList("charms_goblin.yml"));
        setLocalCooldown(60D);
        setGlobalCooldown(15D);
        setWeight(50);
        setCustomSpawn("normal_surface_spawn.yml");
        setEventDuration(60);
    }
}
