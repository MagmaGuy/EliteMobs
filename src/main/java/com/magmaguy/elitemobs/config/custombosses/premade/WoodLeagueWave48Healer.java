package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WoodLeagueWave48Healer extends CustomBossesConfigFields {
    public WoodLeagueWave48Healer() {
        super("wood_league_wave_48_healer",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Healer",
                "48");
        setFollowDistance(60);
        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(List.of("channel_healing.yml"));
        setHealthMultiplier(.5D);
    }
}
