package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave36Melee extends CustomBossesConfigFields {
    public WoodLeagueWave36Melee() {
        super("wood_league_wave_36_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "36");
        setFollowDistance(60);
        setPowers(new ArrayList<>(List.of("corpse.yml", "attack_fire.yml", "invulnerability_arrow.yml")));
        setHelmet(new ItemStack(Material.IRON_HELMET));
        setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        setBoots(new ItemStack(Material.IRON_BOOTS));
        setMainHand(new ItemStack(Material.GOLDEN_SWORD));
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
