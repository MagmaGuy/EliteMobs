package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.ItemConsumables;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ScrapEliteHugeConfig extends CustomItemsConfigFields {
    public ScrapEliteHugeConfig() {
        super("elite_scrap_huge",
                true,
                Material.PURPLE_DYE,
                "&5Huge Elite Scrap",
                new ArrayList<>(List.of("&fUsed to repair Elite items!", "&fFully repairs an item!")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setConsumable(new ItemConsumables.Definition(ItemConsumables.Type.REPAIR_SCRAP, 5));
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
