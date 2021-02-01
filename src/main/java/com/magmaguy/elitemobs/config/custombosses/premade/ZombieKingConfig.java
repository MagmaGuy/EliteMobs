package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class ZombieKingConfig extends CustomBossConfigFields {

    public ZombieKingConfig() {
        super("zombie_king",
                EntityType.ZOMBIE.toString(),
                true,
                "$eventBossLevel &4Zombie King",
                "dynamic",
                30,
                true,
                4,
                4,
                Material.DIAMOND_HELMET,
                Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS,
                Material.DIAMOND_BOOTS,
                Material.GOLDEN_AXE,
                null,
                false,
                Arrays.asList("flame_pyre.yml", "flamethrower.yml", "summon_the_returned.yml", "spirit_walk.yml"),
                "&cThe Zombie King has been sighted!",
                "&aThe Zombie King has been slain by $players!",
                Arrays.asList(
                        "&e&l---------------------------------------------",
                        "&4The Zombie King has been slain!",
                        "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                        "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                        "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                        "&aSlayers: $players",
                        "&e&l---------------------------------------------"),
                "&4The Zombie King has escaped!",
                "&cZombie King: $distance blocks away!",
                Arrays.asList("zombie_kings_axe.yml:1"),
                true,
                true,
                Arrays.asList(Particle.FLAME.toString()),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                2);
    }

}
