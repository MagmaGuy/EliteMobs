package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class LostPresentConfig extends CustomItemsConfigFields {
    public LostPresentConfig() {
        super("xmas_lost_present",
                true,
                Material.CHEST,
                "&2Lost Present",
                new ArrayList<>(List.of("&2Turn this in to Santa for a reward!")));
        setScalability(CustomItem.Scalability.FIXED);
        setItemType(CustomItem.ItemType.UNIQUE);
        setPermission("elitequest.xmas_quest.yml");
    }
}