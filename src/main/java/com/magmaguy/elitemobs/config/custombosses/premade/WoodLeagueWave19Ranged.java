package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave19Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave19Ranged(){
        super("wood_league_wave_19_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Archery Club Enthusiast",
                "19");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setMainHand(new ItemStack(Material.BOW));
        setPowers(Arrays.asList("attack_vacuum.yml", "skeleton_tracking_arrow.yml"));
    }
}
