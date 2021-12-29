package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;

import java.util.Arrays;

public class KillerRabbitOfCaerbannogEvent extends CustomEventsConfigFields {
    public KillerRabbitOfCaerbannogEvent() {
        super("killer_rabbit_of_caerbannog",
                true);
        setEventType(CustomEvent.EventType.TILL_SOIL);
        setBossFilenames(Arrays.asList("killer_rabbit_of_caerbannog.yml"));
        setChance(0.0001);
        setEventDuration(20);
    }
}