package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class FaeEvent extends CustomEventsConfigFields {
    public FaeEvent() {
        super("fae",
                true);
        setEventType(CustomEvent.EventType.BREAK_BLOCK);
        setBossFilenames(new ArrayList<>(List.of("fire_fae.yml", "ice_fae.yml", "lightning_fae.yml")));
        setChance(0.001);
        setBreakableMaterials(new ArrayList<>(List.of(Material.BIRCH_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
                Material.OAK_LOG, Material.SPRUCE_LOG, Material.JUNGLE_LOG)));
        setEventDuration(20);
    }
}
