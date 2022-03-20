package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave10Boss extends CustomBossesConfigFields {
    public WoodLeagueWave10Boss(){
        super("wood_league_wave_10_boss",
                EntityType.WOLF,
                true,
                "$bossLevel &4Bad Doggo",
                "10");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setPowers(Arrays.asList("ground_pound.yml",
                "summonable:summonType=ON_HIT:filename=wood_league_wave_10_reinforcement.yml:amount=2:chance=0.1:inheritAggro=true:spawnNearby=true"));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(5D);
        setDamageMultiplier(2D);
    }
}
