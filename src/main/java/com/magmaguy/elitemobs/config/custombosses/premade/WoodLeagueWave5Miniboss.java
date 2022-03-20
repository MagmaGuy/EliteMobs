package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave5Miniboss extends CustomBossesConfigFields {
    public WoodLeagueWave5Miniboss() {
        super("wood_league_wave_5_miniboss",
                EntityType.RAVAGER,
                true,
                "$minibossLevel &cWeird Cow",
                "5");
        setPowers(Arrays.asList("attack_push.yml", "attack_gravity.yml"));
        setOnDamagedMessages(Arrays.asList("Woof!"));
        setHealthMultiplier(3D);
        setDamageMultiplier(2D);
        setFollowDistance(60);
        setMovementSpeedAttribute(0.6D);
        setHelmet(new ItemStack(Material.STICK));
    }
}