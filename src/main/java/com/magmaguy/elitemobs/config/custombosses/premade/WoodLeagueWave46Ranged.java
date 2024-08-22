package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave46Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave46Ranged() {
        super("wood_league_wave_46_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Crossbowman",
                "46");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(new ArrayList<>(List.of("attack_freeze.yml", "skeleton_tracking_arrow.yml", "shield_wall.yml")));
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
