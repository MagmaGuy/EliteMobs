package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class TheStingerConfig extends CustomItemsConfigFields {
    public TheStingerConfig() {
        super("the_stinger",
                true,
                Material.GOLDEN_SWORD,
                "&eThe Stinger",
                Arrays.asList("&aFloat like a butterfly,", "&asting like a bee!"));
        setEnchantments(Arrays.asList("DAMAGE_ALL,1", "DURABILITY,1", "VANISHING_CURSE,1"));
        setPotionEffects(List.of("POISON,0,target,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
