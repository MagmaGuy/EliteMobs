package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.Arrays;

public class KrakenEvent extends CustomEventsConfigFields {
    public KrakenEvent() {
        super("kraken",
                true,
                CustomEvent.EventType.FISH,
                Arrays.asList("kraken.yml"));
        setChance(0.0001);
    }
}
