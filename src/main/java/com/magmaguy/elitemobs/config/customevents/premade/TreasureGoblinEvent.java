package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.Arrays;

public class TreasureGoblinEvent extends CustomEventsConfigFields {
    public TreasureGoblinEvent() {
        super("treasure_goblin",
                true);
        setEventType(CustomEvent.EventType.TIMED);
        setBossFilenames(Arrays.asList("treasure_goblin.yml"));
        setLocalCooldown(60D);
        setGlobalCooldown(15D);
        setWeight(100D);
        setCustomSpawn("treasure_goblin_spawn.yml");
    }
}
