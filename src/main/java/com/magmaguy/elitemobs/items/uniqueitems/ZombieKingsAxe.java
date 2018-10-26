package com.magmaguy.elitemobs.items.uniqueitems;

import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantmentCache;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class ZombieKingsAxe extends UniqueItem {

    @Override
    public String definePath() {
        return "Zombie Kings Axe";
    }

    @Override
    public String defineType() {
        return Material.GOLD_AXE.toString();
    }

    @Override
    public String defineName() {
        return "&4Zombie King's Axe";
    }

    @Override
    public List<String> defineLore() {
        return Arrays.asList("The axe of the one Zombies", "call their king.", "The bloodlust is palpable.");
    }

    @Override
    public List<String> defineEnchantments() {
        return Arrays.asList(
                "DAMAGE_ALL,10",
                "DAMAGE_UNDEAD,5",
                "DIG_SPEED,5",
                "DURABILITY,5",
                "KNOCKBACK,3",
                "FIRE_ASPECT,4",
                "LOOT_BONUS_MOBS,5",
                "WATER_WORKER,5",
                CustomEnchantmentCache.flamethrowerEnchantment.assembleConfigString(1)
        );
    }

    @Override
    public List<String> definePotionEffects() {
        return Arrays.asList(
                "FAST_DIGGING,1,self,onHit",
                "POISON,1,target,onHit",
                "GLOWING,1,target,onHit",
                "NIGHT_VISION,1,target,continuous",
                "WITHER,1,target,onHit"
        );
    }

    @Override
    public String defineDropWeight() {
        return "unique";
    }

    @Override
    public String defineScalability() {
        return "dynamic";
    }

}
