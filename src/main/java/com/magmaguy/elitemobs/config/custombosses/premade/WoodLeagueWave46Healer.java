package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave46Healer extends CustomBossesConfigFields {
    public WoodLeagueWave46Healer(){
        super("wood_league_wave_46_healer",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Healer",
                "46");
        setFollowDistance(60);
        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(Arrays.asList("channel_healing.yml"));
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
        setHealthMultiplier(.5D);
    }
}
