package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class ArmorGoblinBoss extends CustomBossesConfigFields {
    public ArmorGoblinBoss(){
        super("armor_goblin",
                EntityType.ZOMBIE,
                true,
                "$eventBossLevel &3Armor Goblin",
                "dynamic");
        setHealthMultiplier(4);
        setDamageMultiplier(4);
        setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        setMainHand(new ItemStack(Material.SHIELD));
        setOffHand(new ItemStack(Material.SHIELD));
        setBaby(true);
        setPowers(Arrays.asList("gold_explosion.yml", "gold_shotgun.yml", "spirit_walk.yml"));
        setSpawnMessage("&cA Armor Goblin has been sighted!");
        setDeathMessage("&aA Armor Goblin has been slain by $players!");
        setDeathMessages(Arrays.asList(
                "&e&l---------------------------------------------",
                "&eThe Armor Goblin has been pillaged!",
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------"));
        setEscapeMessage("&4A Armor Goblin has escaped!");
        setLocationMessage("&cArmor Goblin: $distance blocks away!");
        setTrails(Collections.singletonList(Material.GOLD_NUGGET.toString()));
        setAnnouncementPriority(2);
        setPersistent(true);
        setUniqueLootList(Arrays.asList(
                "goblin_helmet.yml:0.2",
                "goblin_chestplate.yml:0.2",
                "goblin_leggings.yml:0.2",
                "goblin_boots.yml:0.2"
                ));
        setFollowDistance(100);
    }
}
