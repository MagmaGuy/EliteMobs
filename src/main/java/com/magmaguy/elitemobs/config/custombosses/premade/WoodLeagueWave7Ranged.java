package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave7Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave7Ranged() {
        super("wood_league_wave_7_ranged",
                EntityType.SKELETON,
                true,
                "$normalLevel Arena Skeleton",
                "7");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setMainHand(new ItemStack(Material.BOW));
        setPowers(Arrays.asList("attack_vacuum.yml"));
    }
}
