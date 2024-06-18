package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class DwarvenGreedConfig extends CustomItemsConfigFields {
    public DwarvenGreedConfig() {
        super("dwarven_greed",
                true,
                Material.WOODEN_PICKAXE,
                "&4Dwarven Greed",
                Arrays.asList("&cThose who delve too greedily", "&cand too deep may wake ancient", "&chorrors of shadow and flame", "&cbest left undisturbed."));
        setEnchantments(Arrays.asList("LOOT_BONUS_BLOCKS,4", "UNBREAKING,6", "DIG_SPEED,6", "VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("FAST_DIGGING,1,self,continuous", "NIGHT_VISION,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
