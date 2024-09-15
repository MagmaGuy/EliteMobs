package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave25Miniboss extends CustomBossesConfigFields {
    public WoodLeagueWave25Miniboss() {
        super("wood_league_wave_25_miniboss",
                EntityType.PILLAGER,
                true,
                "$minibossLevel &bAgdluak",
                "25");
        setPowers(new ArrayList<>(List.of("frost_cone.yml", "attack_freeze.yml", "arrow_rain.yml")));
        setHealthMultiplier(3D);
        setDamageMultiplier(2D);
        setFollowDistance(60);
        setMovementSpeedAttribute(0.6D);
        setMainHand(new ItemStack(Material.CROSSBOW));
    }
}
