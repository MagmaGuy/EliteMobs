package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class WoodLeagueWave10Reinforcement extends CustomBossesConfigFields {
    public WoodLeagueWave10Reinforcement(){
        super("wood_league_wave_10_reinforcement",
                EntityType.WOLF,
                true,
                "$reinforcementLevel &cBad Pupper",
                "10");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(0.25D);
        setDamageMultiplier(0.8D);
        setDropsRandomLoot(false);
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setNormalizedCombat(true);
    }
}
