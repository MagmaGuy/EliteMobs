package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.ItemConsumables;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ScrapEliteSmallConfig extends CustomItemsConfigFields {
    public ScrapEliteSmallConfig() {
        super("elite_scrap_small",
                true,
                Material.GREEN_DYE,
                "&9Small Elite Scrap",
                new ArrayList<>(List.of("&fUsed to repair Elite items!", "&fRepairs a medium amount!")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setConsumable(new ItemConsumables.Definition(ItemConsumables.Type.REPAIR_SCRAP, 2));
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
