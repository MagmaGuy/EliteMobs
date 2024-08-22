package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave47Healer extends CustomBossesConfigFields {
    public WoodLeagueWave47Healer() {
        super("wood_league_wave_47_healer",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Healer",
                "47");
        setFollowDistance(60);
        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(new ArrayList<>(List.of("channel_healing.yml")));
        setHealthMultiplier(.5D);
    }
}
