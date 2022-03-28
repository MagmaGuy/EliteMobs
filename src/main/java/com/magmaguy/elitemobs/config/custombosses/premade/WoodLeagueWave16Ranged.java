package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave16Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave16Ranged(){
        super("wood_league_wave_16_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Mr. Oinkers Fan",
                "16");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setMainHand(new ItemStack(Material.BOW));
        setPowers(Arrays.asList("attack_vacuum.yml"));
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
