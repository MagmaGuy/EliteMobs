package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave12Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave12Ranged(){
        super("wood_league_wave_12_ranged",
                EntityType.SKELETON,
                true,
                "$normalLevel Arena Skeleton",
                "12");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setMainHand(new ItemStack(Material.BOW));
        setPowers(Arrays.asList("attack_vacuum.yml"));
    }
}
