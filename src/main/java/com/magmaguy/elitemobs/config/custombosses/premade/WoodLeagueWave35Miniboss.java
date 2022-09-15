package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave35Miniboss extends CustomBossesConfigFields {
    public WoodLeagueWave35Miniboss() {
        super("wood_league_wave_35_miniboss",
                EntityType.VINDICATOR,
                true,
                "$minibossLevel &6The Jester",
                "35");
        setPowers(Arrays.asList("attack_confusing.yml", "attack_push.yml", "attack_gravity.yml", "fireworks_barrage.yml", "arrow_fireworks.yml"));
        setHealthMultiplier(3D);
        setDamageMultiplier(2D);
        setFollowDistance(60);
        setMovementSpeedAttribute(0.6D);
        setMainHand(new ItemStack(Material.GOLDEN_AXE));
    }
}
