package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.ItemConsumables;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ScrapEliteLargeConfig extends CustomItemsConfigFields {
    public ScrapEliteLargeConfig() {
        super("elite_scrap_large",
                true,
                Material.RED_DYE,
                "&9Large Elite Scrap",
                new ArrayList<>(List.of("&fUsed to repair Elite items!", "&fRepairs a huge amount!")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setConsumable(new ItemConsumables.Definition(ItemConsumables.Type.REPAIR_SCRAP, 4));
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
