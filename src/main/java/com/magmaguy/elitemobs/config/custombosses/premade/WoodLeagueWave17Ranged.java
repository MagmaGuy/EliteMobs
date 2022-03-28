package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave17Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave17Ranged(){
        super("wood_league_wave_17_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Crossbowman",
                "17");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setMainHand(new ItemStack(Material.BOW));
        setPowers(Arrays.asList("attack_vacuum.yml", "skeleton_tracking_arrow.yml"));
    }
}
