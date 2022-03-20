package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class CoinsGoblinBoss extends CustomBossesConfigFields {
    public CoinsGoblinBoss() {
        super("coins_goblin",
                EntityType.ZOMBIE,
                true,
                "$eventBossLevel &eCoins Goblin",
                "dynamic");
        setHealthMultiplier(4);
        setDamageMultiplier(4);
        setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
        setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
        setBoots(new ItemStack(Material.GOLDEN_BOOTS));
        setBaby(true);
        setPowers(Arrays.asList("gold_explosion.yml", "gold_shotgun.yml", "spirit_walk.yml", "bonus_coins.yml:20"));
        setSpawnMessage("&cA Coins Goblin has been sighted!");
        setDeathMessage("&aA Coins Goblin has been slain by $players!");
        setDeathMessages(Arrays.asList(
                "&e&l---------------------------------------------",
                "&eThe Coins Goblin has been pillaged!",
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------"));
        setEscapeMessage("&4A Coins Goblin has escaped!");
        setLocationMessage("&cCoins Goblin: $distance blocks away!");
        setTrails(Collections.singletonList(Material.GOLD_NUGGET.toString()));
        setAnnouncementPriority(2);
        setPersistent(true);
        setFollowDistance(100);
    }
}
