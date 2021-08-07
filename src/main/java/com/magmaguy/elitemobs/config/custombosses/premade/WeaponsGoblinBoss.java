package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class WeaponsGoblinBoss extends CustomBossesConfigFields {
    public WeaponsGoblinBoss() {
        super("weapons_goblin",
                EntityType.ZOMBIE,
                true,
                "$eventBossLevel &cWeapons Goblin",
                "dynamic");
        setHealthMultiplier(4);
        setDamageMultiplier(4);
        setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
        setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        setMainHand(new ItemStack(Material.NETHERITE_SWORD));
        setOffHand(new ItemStack(Material.NETHERITE_AXE));
        setBaby(true);
        setPowers(Arrays.asList("gold_explosion.yml", "gold_shotgun.yml", "spirit_walk.yml"));
        setSpawnMessage("&cA Weapons Goblin has been sighted!");
        setDeathMessage("&aA Weapons Goblin has been slain by $players!");
        setDeathMessages(Arrays.asList(
                "&e&l---------------------------------------------",
                "&eThe Weapons Goblin has been pillaged!",
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------"));
        setEscapeMessage("&4A Weapons Goblin has escaped!");
        setLocationMessage("&cWeapons Goblin: $distance blocks away!");
        setTrails(Collections.singletonList(Material.GOLD_NUGGET.toString()));
        setAnnouncementPriority(2);
        setPersistent(true);
        setUniqueLootList(Arrays.asList(
                "goblin_slasher.yml:0.2",
                "goblin_cleaver.yml:0.2",
                "goblin_poker.yml:0.2",
                "goblin_shooter.yml:0.2",
                "goblin_ballista.yml:0.2"));
        setFollowDistance(100);
    }
}
