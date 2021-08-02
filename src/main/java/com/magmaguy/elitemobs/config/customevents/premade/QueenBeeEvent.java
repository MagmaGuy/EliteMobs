package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;
import org.bukkit.Material;

import java.util.Arrays;

public class QueenBeeEvent extends CustomEventsConfigFields {
    public QueenBeeEvent(){
        super("queen_bee",
                 true);
        setEventType(CustomEvent.EventType.BREAK_BLOCK);
        setBossFilenames(Arrays.asList("queen_bee.yml"));
        setChance(0.001);
        setBreakableMaterials(Arrays.asList(Material.BEE_NEST));
    }
}
