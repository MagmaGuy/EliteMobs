package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class TotemZombie1Boss extends CustomBossesConfigFields {
    public TotemZombie1Boss() {
        super("totem_zombie_1", EntityType.ZOMBIE, true, "$eventBossLevel Head Zombie", "dynamic");
        setPersistent(true);
        setMountedEntity("totem_zombie_2.yml");
        setPowers(Arrays.asList("skeleton_tracking_arrow.yml", "tracking_fireball.yml", "hyper_loot.yml"));
        setCullReinforcements(false);
        setFollowDistance(100);
        setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
        setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        setMainHand(new ItemStack(Material.NETHERITE_SWORD));
        setDamageMultiplier(2);
        setHealthMultiplier(2);
        setSpawnMessage("&cA Dr. Craftenmine abomination has been sighted!");
        setDeathMessage("&aDr. Craftenmine's abomination been terminated by $players!");
        setDeathMessages(Arrays.asList(
                "&e&l---------------------------------------------",
                "&4The Zombie Totem been slain!",
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------"));
        setEscapeMessage("&4Dr. Craftenmine's creation has escaped!");
        setLocationMessage("&cZombie Totem: $distance blocks away!");
        setAnnouncementPriority(2);
    }
}
