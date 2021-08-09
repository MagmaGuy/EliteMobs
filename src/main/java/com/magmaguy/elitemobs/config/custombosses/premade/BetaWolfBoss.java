package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.HashMap;

public class BetaWolfBoss extends CustomBossesConfigFields {
    public BetaWolfBoss() {
        super("beta_wolf",
                EntityType.WOLF,
                true,
                "$reinforcementLevel &7Beta Wolf",
                "dynamic");
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setHealthMultiplier(0.5);
        setDamageMultiplier(0.5);
        setPowers(Arrays.asList("attack_poison.yml"));
        HashMap<Material, Double> damageModifiers = new HashMap<>();
        damageModifiers.put(Material.IRON_SWORD, 2D);
        damageModifiers.put(Material.IRON_AXE, 2D);
        setDamageModifiers(damageModifiers);
        setFollowDistance(100);
    }
}
