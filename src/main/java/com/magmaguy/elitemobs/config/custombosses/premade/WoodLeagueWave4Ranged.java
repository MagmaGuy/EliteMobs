package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class WoodLeagueWave4Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave4Ranged(){
        super("wood_league_wave_4_ranged",
                EntityType.SKELETON,
                true,
                "$normalLevel Arena Skeleton",
                "4");
        setMainHand(new ItemStack(Material.BOW));
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
    }
}
