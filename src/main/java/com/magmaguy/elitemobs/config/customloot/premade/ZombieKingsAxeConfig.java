package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class ZombieKingsAxeConfig extends CustomLootConfigFields {
    public ZombieKingsAxeConfig() {
        super("zombie_kings_axe",
                true,
                Material.GOLDEN_AXE.toString(),
                "&4Zombie King's Axe",
                Arrays.asList("The axe of the one Zombies", "call their king.", "The bloodlust is palpable."),
                Arrays.asList("DAMAGE_ALL,10", "DAMAGE_UNDEAD,5", "DIG_SPEED,5", "DURABILITY,5", "KNOCKBACK,3",
                        "FIRE_ASPECT,4", "LOOT_BONUS_MOBS,5", "WATER_WORKER,5", "FLAMETHROWER,1"),
                Arrays.asList("FAST_DIGGING,1,self,onHit", "POISON,1,target,onHit", "GLOWING,1,target,onHit",
                        "NIGHT_VISION,1,target,continuous", "WITHER,1,target,onHit"),
                "dynamic",
                "scalable",
                "unique");
    }
}
