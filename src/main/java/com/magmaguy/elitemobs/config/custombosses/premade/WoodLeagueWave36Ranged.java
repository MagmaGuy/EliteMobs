package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave36Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave36Ranged(){
        super("wood_league_wave_36_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Crossbowman",
                "36");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.IRON_HELMET));
        setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        setBoots(new ItemStack(Material.IRON_BOOTS));
        setMainHand(new ItemStack(Material.BOW));
        setPowers(Arrays.asList("attack_freeze.yml", "skeleton_tracking_arrow.yml"));
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
