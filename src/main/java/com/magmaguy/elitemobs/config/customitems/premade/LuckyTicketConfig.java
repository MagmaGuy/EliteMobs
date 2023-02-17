package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class LuckyTicketConfig extends CustomItemsConfigFields {
    public LuckyTicketConfig() {
        super("elite_lucky_ticket",
                true,
                Material.PAPER,
                "&6Elite Lucky Ticket",
                new ArrayList<>(List.of(
                        "&2Doubles the chance of successfully",
                        "&2enchanting an item!")));
        setEnchantments(List.of("LUCKY_SOURCE,1"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
