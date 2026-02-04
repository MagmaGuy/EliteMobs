package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCustomBossConfig extends CustomBossesConfigFields {
    public TestCustomBossConfig() {
        super("test_boss",
                EntityType.ZOMBIE,
                true,
                "$eventBossLevel &6Test boss",
                "dynamic");
        setTimeout(10);
        setHealthMultiplier(2);
        setDamageMultiplier(0.5);
        setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
        setMainHand(new ItemStack(Material.GOLDEN_AXE));
        setOffHand(new ItemStack(Material.SHIELD));
        setPowers(new ArrayList<>(List.of("invulnerability_knockback.yml")));
        setSpawnMessage("A test boss has been spawned!");
        setDeathMessage("A test boss has been slain by $players!");
        setDeathMessages(new ArrayList<>(List.of(
                "&e&l---------------------------------------------",
                "&eThe Test Boss has been debugged!",
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------")));
        setEscapeMessage("A test boss entity has escaped!");
        setLocationMessage("Test entity: $distance");
        setUniqueLootList(Collections.singletonList("magmaguys_toothpick.yml:1"));
        setTrails(Collections.singletonList(Particle.BLOCK_MARKER.toString()));
        setOnDamageMessages(Collections.singletonList("I've hit you!"));
        setOnDamagedMessages(Collections.singletonList("I've been hit!"));
        setOnDeathCommands(Collections.singletonList("say $players has killed $name! That was level $level!"));
        setAnnouncementPriority(3);
    }
}
