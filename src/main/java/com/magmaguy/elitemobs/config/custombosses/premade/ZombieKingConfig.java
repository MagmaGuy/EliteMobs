package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class ZombieKingConfig extends CustomBossConfigFields {
    public ZombieKingConfig() {
        super("zombie_king",
                EntityType.ZOMBIE.toString(),
                true,
                "$eventBossLevel &4Zombie King",
                "dynamic");
        setTimeout(30);
        setPersistent(true);
        setHealthMultiplier(4);
        setDamageMultiplier(4);
        setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        setPowers(Arrays.asList("flame_pyre.yml", "flamethrower.yml", "summon_the_returned.yml", "spirit_walk.yml"));
        setSpawnMessage("&cThe Zombie King has been sighted!");
        setDeathMessage("&aThe Zombie King has been slain by $players!");
        setDeathMessages(Arrays.asList(
                "&e&l---------------------------------------------",
                "&4The Zombie King has been slain!",
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------"));
        setEscapeMessage("&4The Zombie King has escaped!");
        setLocationMessage("&cZombie King: $distance blocks away!");
        setUniqueLootList(Collections.singletonList("zombie_kings_axe.yml:1"));
        setTrails(Collections.singletonList(Particle.FLAME.toString()));
        setAnnouncementPriority(2);
    }
}
