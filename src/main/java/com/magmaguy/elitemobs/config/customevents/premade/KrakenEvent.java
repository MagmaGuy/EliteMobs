package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.List;

public class KrakenEvent extends CustomEventsConfigFields {
    public KrakenEvent() {
        super("kraken",
                true);
        setEventType(CustomEvent.EventType.FISH);
        setBossFilenames(List.of("kraken.yml"));
        setChance(0.005);
        setEventDuration(20);
    }
}
