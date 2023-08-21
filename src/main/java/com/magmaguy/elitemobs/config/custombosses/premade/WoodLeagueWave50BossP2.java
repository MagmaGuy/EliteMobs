package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave50BossP2 extends CustomBossesConfigFields {
    public WoodLeagueWave50BossP2() {
        super("wood_league_wave_50_boss_p2",
                EntityType.WITHER_SKELETON,
                true,
                "$bossLevel &6Uther the Champion",
                "50");
        setFollowDistance(60);
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMainHand(new ItemStack(Material.NETHERITE_AXE));
        setOffHand(new ItemStack(Material.SHIELD));
        setPowers(Arrays.asList("firestorm.yml",
                "flame_pyre.yml",
                "flamethrower.yml",
                "death_slice.yml",
                "firestorm.yml",
                "summonable:summonType=ON_COMBAT_ENTER:filename=wood_league_wave_50_reinforcement.yml:amount=2"));
        setMovementSpeedAttribute(0.5D);
        setHealthMultiplier(10D);
        setDamageMultiplier(1D);
    }
}
