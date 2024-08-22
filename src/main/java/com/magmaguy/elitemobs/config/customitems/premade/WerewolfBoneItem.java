package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class WerewolfBoneItem extends CustomItemsConfigFields {
    public WerewolfBoneItem() {
        super("werewolf_bone",
                true,
                Material.BONE,
                "&6Werewolf Bone",
                new ArrayList<>(List.of("&aHaving killed the alpha,", "&athis temporarily makes you!", "&athe wolves' new leader")));
        setEnchantments(List.of("SUMMON_WOLF,1"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
