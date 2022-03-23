package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave15Miniboss extends CustomBossesConfigFields {
    public WoodLeagueWave15Miniboss(){
        super("wood_league_wave_15_miniboss",
                EntityType.ZOGLIN,
                true,
                "$bossLevel &4Mr. Oinkers",
                "15");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setPowers(Arrays.asList("gold_explosion.yml", "gold_shotgun.yml"));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(3D);
        setDamageMultiplier(2D);
        setOnDamagedMessages(Arrays.asList("Oink!?"));
    }
}
