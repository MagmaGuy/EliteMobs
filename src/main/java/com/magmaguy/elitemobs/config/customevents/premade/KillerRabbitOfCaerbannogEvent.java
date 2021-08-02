package com.magmaguy.elitemobs.config.customevents.premade;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;
import org.bukkit.Material;

import java.util.Arrays;

public class KillerRabbitOfCaerbannogEvent extends CustomEventsConfigFields {
    public KillerRabbitOfCaerbannogEvent() {
        super("killer_rabbit_of_caerbannog",
                true);
        setEventType(CustomEvent.EventType.TILL_SOIL);
        setBossFilenames(Arrays.asList("killer_rabbit_of_caerbannog.yml"));
        setChance(0.0001);
        setBreakableMaterials(Arrays.asList(Material.COAL, Material.IRON_ORE, Material.GOLD_ORE, Material.LAPIS_ORE,
                Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.REDSTONE_ORE));
    }
}