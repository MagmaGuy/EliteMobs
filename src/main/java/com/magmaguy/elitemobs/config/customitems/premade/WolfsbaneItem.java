package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class WolfsbaneItem extends CustomItemsConfigFields {
    public WolfsbaneItem(){
        super("wolfsbane",
                true,
                Material.IRON_SWORD,
                "&7Wolfsbane",
                Arrays.asList("&aMade from the remains of", "&aan alpha werewolf, all", "&awolves cower at the sight of it."));
        setEnchantments(Arrays.asList("DAMAGE_ALL,1", "FIRE_ASPECT,4", "DURABILITY,5", "LOOT_BONUS_MOBS,5"));
        setPotionEffects(Arrays.asList("FAST_DIGGING,2,self,onHit",  "NIGHT_VISION,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
