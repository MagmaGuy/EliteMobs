package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class WoodLeagueWave50Boss extends CustomBossesConfigFields {
    public WoodLeagueWave50Boss() {
        super("wood_league_wave_50_boss",
                EntityType.WITHER_SKELETON,
                true,
                "$bossLevel &6Uther the Champion",
                "50");
        setFollowDistance(60);
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMainHand(new ItemStack(Material.NETHERITE_AXE));
        setOffHand(new ItemStack(Material.SHIELD));
        setPowers(Arrays.asList("ground_pound.yml",
                "shield_wall.yml",
                "bullet_hell.yml",
                "arrow_fireworks.yml",
                "fireworks_barrage.yml",
                "summonable:summonType=ON_COMBAT_ENTER:filename=wood_league_wave_50_reinforcement.yml:amount=2"));
        setMovementSpeedAttribute(0.4D);
        setHealthMultiplier(10D);
        setDamageMultiplier(1D);
        setPhases(new ArrayList<>(Arrays.asList(
                "wood_league_wave_50_boss_p2.yml:0.66",
                "wood_league_wave_50_boss_p3.yml:0.33")));
    }
}
