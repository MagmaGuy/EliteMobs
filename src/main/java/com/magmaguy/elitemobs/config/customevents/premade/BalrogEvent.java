package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class BalrogEvent extends CustomEventsConfigFields {
    public BalrogEvent() {
        super("balrog",
                true);
        setEventType(CustomEvent.EventType.BREAK_BLOCK);
        setBossFilenames(List.of("balrog.yml"));
        setChance(0.001);
        setBreakableMaterials(new ArrayList<>(List.of(Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.LAPIS_ORE,
                Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.REDSTONE_ORE)));
        setEventDuration(20);
    }
}
