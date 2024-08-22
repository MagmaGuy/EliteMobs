package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class TheFellerConfig extends CustomItemsConfigFields {
    public TheFellerConfig() {
        super("the_feller",
                true,
                Material.DIAMOND_AXE,
                "&2The Feller",
                new ArrayList<>(List.of("&aEven in your sleep,", "&ayou can feel this axe''s", "&asaplust")));
        setEnchantments(new ArrayList<>(List.of("FORTUNE,4", "SILK_TOUCH,1", "UNBREAKING,6", "EFFICIENCY,6", "VANISHING_CURSE,1")));
        setPotionEffects(new ArrayList<>(List.of("FAST_DIGGING,1,self,continuous", "NIGHT_VISION,0,self,continuous")));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
