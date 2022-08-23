package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class ZombieKingsAxeConfig extends CustomItemsConfigFields {
    public ZombieKingsAxeConfig() {
        super("zombie_kings_axe",
                true,
                Material.GOLDEN_AXE,
                "&4Zombie King's Axe",
                Arrays.asList("The axe of the one Zombies", "call their king.", "The bloodlust is palpable."));
        setEnchantments(Arrays.asList("DAMAGE_ALL,1", "DAMAGE_UNDEAD,5", "DIG_SPEED,5", "DURABILITY,5", "KNOCKBACK,3",
                "FIRE_ASPECT,4", "LOOT_BONUS_MOBS,5", "WATER_WORKER,5", "FLAMETHROWER,1"));
        setPotionEffects(Arrays.asList("FAST_DIGGING,0,self,onHit", "NIGHT_VISION,0,self,continuous", "WITHER,0,target,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
