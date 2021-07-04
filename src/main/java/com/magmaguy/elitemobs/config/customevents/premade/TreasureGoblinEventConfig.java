package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.events.TimedEvent;

import java.util.Arrays;

public class TreasureGoblinEventConfig extends CustomEventsConfigFields {
    public TreasureGoblinEventConfig(){
        super("treasure_goblin",
                true,
                CustomEvent.EventType.TIMED,
                Arrays.asList("treasure_goblin.yml"),
                60d,
                15d,
                100,
                TimedEvent.SpawnType.INSTANT_SPAWN);
    }
}
