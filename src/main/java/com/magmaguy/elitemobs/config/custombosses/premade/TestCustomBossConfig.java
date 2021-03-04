package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class TestCustomBossConfig extends CustomBossConfigFields {
    public TestCustomBossConfig() {
        super("test_boss",
                EntityType.ZOMBIE.toString(),
                true,
                "$eventBossLevel &eTest boss",
                "dynamic",
                10,
                false,
                2,
                0.5,
                Material.GOLDEN_HELMET,
                Material.IRON_CHESTPLATE,
                Material.LEATHER_LEGGINGS,
                Material.CHAINMAIL_BOOTS,
                Material.GOLDEN_AXE,
                Material.SHIELD,
                false,
                Arrays.asList("invulnerability_knockback.yml"),
                "A test boss has been spawned!",
                "A test boss has been slain by $players!",
                null,
                "A test boss entity has escaped!",
                "Test entity: $distance",
                Arrays.asList("magmaguys_toothpick.yml:1"),
                true,
                true,
                Arrays.asList(Particle.BARRIER.toString()),
                Arrays.asList("I've hit you!"),
                Arrays.asList("I've been hit!"),
                0D,
                false,
                0,
                0D,
                Arrays.asList("broadcast $players has killed $name! That was level $level!"),
                1);
    }
}
