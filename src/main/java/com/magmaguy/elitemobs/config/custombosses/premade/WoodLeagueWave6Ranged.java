package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class WoodLeagueWave6Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave6Ranged(){
        super("wood_league_wave_6_ranged",
                EntityType.SKELETON,
                true,
                "$normalLevel Arena Skeleton",
                "6");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setMainHand(new ItemStack(Material.BOW));
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
