package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;
import org.bukkit.Material;

import java.util.Arrays;

public class BalrogEvent extends CustomEventsConfigFields {
    public BalrogEvent() {
        super("balrog",
                true);
        setEventType(CustomEvent.EventType.BREAK_BLOCK);
        setBossFilenames(Arrays.asList("balrog.yml"));
        setChance(0.0001);
        setBreakableMaterials(Arrays.asList(Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.LAPIS_ORE,
                Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.REDSTONE_ORE));
    }
}
