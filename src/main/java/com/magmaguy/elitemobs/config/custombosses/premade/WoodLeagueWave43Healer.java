package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave43Healer extends CustomBossesConfigFields {
    public WoodLeagueWave43Healer() {
        super("wood_league_wave_43_healer",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Healer",
                "43");
        setFollowDistance(60);
        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(new ArrayList<>(List.of("channel_healing.yml")));
        setHealthMultiplier(.5D);
    }
}
