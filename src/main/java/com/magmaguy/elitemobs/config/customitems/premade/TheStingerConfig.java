package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class TheStingerConfig extends CustomItemsConfigFields {
    public TheStingerConfig() {
        super("the_stinger",
                true,
                Material.GOLDEN_SWORD,
                "&eThe Stinger",
                new ArrayList<>(List.of("&aFloat like a butterfly,", "&asting like a bee!")));
        setEnchantments(new ArrayList<>(List.of("SHARPNESS,1", "UNBREAKING,1", "VANISHING_CURSE,1")));
        setPotionEffects(List.of("POISON,0,target,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
