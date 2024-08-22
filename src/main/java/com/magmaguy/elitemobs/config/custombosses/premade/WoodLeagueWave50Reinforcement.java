package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave50Reinforcement extends CustomBossesConfigFields {
    public WoodLeagueWave50Reinforcement() {
        super("wood_league_wave_50_reinforcement",
                EntityType.PILLAGER,
                true,
                "$reinforcementLevel &6Uther Stan",
                "50");
        setFollowDistance(60);
        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(new ArrayList<>(List.of("channel_healing.yml")));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(0.25D);
        setDamageMultiplier(0.8D);
        setDropsRandomLoot(false);
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setNormalizedCombat(true);
    }
}
