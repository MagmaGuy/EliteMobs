package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class WoodLeagueWave15Miniboss extends CustomBossesConfigFields {
    public WoodLeagueWave15Miniboss() {
        super("wood_league_wave_15_miniboss",
                EntityType.ZOMBIE,
                true,
                "$bossLevel &4Mr. Oinkers",
                "15");
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setEntityType(EntityType.ZOGLIN);
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setPowers(Arrays.asList("gold_explosion.yml", "gold_shotgun.yml"));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(3D);
        setDamageMultiplier(2D);
        setOnDamagedMessages(List.of("Oink!?"));
    }
}
