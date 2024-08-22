package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class WolfsbaneItem extends CustomItemsConfigFields {
    public WolfsbaneItem() {
        super("wolfsbane",
                true,
                Material.IRON_SWORD,
                "&7Wolfsbane",
                new ArrayList<>(List.of("&aMade from the remains of", "&aan alpha werewolf, all", "&awolves cower at the sight of it.")));
        setEnchantments(new ArrayList<>(List.of("SHARPNESS,1", "FIRE_ASPECT,4", "UNBREAKING,5", "LOOTING,5")));
        setPotionEffects(new ArrayList<>(List.of("FAST_DIGGING,2,self,onHit", "NIGHT_VISION,0,self,continuous")));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
