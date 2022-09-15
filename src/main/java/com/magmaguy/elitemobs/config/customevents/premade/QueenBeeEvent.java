package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;

import java.util.List;

public class QueenBeeEvent extends CustomEventsConfigFields {
    public QueenBeeEvent() {
        super("queen_bee",
                true);
        setEventType(CustomEvent.EventType.BREAK_BLOCK);
        setBossFilenames(List.of("queen_bee.yml"));
        setChance(0.001);
        if (!VersionChecker.serverVersionOlderThan(15, 0))
            setBreakableMaterials(List.of(Material.BEE_NEST));
        setEventDuration(20);
    }
}
