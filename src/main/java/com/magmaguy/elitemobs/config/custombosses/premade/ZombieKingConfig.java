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
                "&4Zombie King",
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
                "&4The Zombie King has escaped!",
                "&cZombie King: $location",
                Arrays.asList("zombie_kings_axe.yml:1"),
                true,
                true,
                Arrays.asList(Particle.FLAME.toString()),
                null,
                null);
    }

}
