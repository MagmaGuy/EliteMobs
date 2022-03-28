package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave45Miniboss extends CustomBossesConfigFields {
    public WoodLeagueWave45Miniboss(){
        super("wood_league_wave_45_miniboss",
                EntityType.VINDICATOR,
                true,
                "$minibossLevel &6Thousand Blades",
                "45");
        setPowers(Arrays.asList("attack_arrow.yml", "arrow_rain.yml", "arrow_fireworks.yml", "attack_fireball.yml", "bullet_hell.yml", "meteor_shower.yml"));
        setHealthMultiplier(3D);
        setDamageMultiplier(2D);
        setFollowDistance(60);
        setMovementSpeedAttribute(0.6D);
        setMainHand(new ItemStack(Material.GOLDEN_AXE));
    }
}
