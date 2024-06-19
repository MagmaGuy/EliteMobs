package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave50BossP3 extends CustomBossesConfigFields {
    public WoodLeagueWave50BossP3() {
        super("wood_league_wave_50_boss_p3",
                EntityType.WITHER_SKELETON,
                true,
                "$bossLevel &6Uther the Champion",
                "50");
        setFollowDistance(60);
        setMainHand(new ItemStack(Material.NETHERITE_AXE));
        setOffHand(new ItemStack(Material.SHIELD));
        setPowers(Arrays.asList("attack_lightning.yml",
                "lightning_bolts.yml",
                "gold_explosion.yml",
                "photon_ray.yml",
                "thunderstorm.yml",
                "summonable:summonType=ON_COMBAT_ENTER:filename=wood_league_wave_50_reinforcement.yml:amount=2"));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(10D);
        setDamageMultiplier(1D);
    }
}
