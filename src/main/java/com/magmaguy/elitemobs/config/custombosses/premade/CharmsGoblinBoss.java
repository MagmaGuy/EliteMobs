package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CharmsGoblinBoss extends CustomBossesConfigFields {
    public CharmsGoblinBoss() {
        super("charms_goblin.yml",
                EntityType.ZOMBIE,
                true,
                "$eventBossLevel &2Charms Goblin",
                "dynamic");
        setHealthMultiplier(4);
        setDamageMultiplier(1.25);
        setHelmet(new ItemStack(Material.IRON_HELMET));
        setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        setBoots(new ItemStack(Material.IRON_BOOTS));
        setBaby(true);
        setPowers(new ArrayList<>(List.of("gold_explosion.yml", "gold_shotgun.yml", "spirit_walk.yml")));
        setSpawnMessage("&cA Charms Goblin has been sighted!");
        setDeathMessage("&aA Charms Goblin has been slain by $players!");
        setDeathMessages(new ArrayList<>(List.of(
                "&e&l---------------------------------------------",
                "&eThe Charms Goblin has been pillaged!",
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------")));
        setEscapeMessage("&4A Charms Goblin has escaped!");
        setLocationMessage("&cCharms Goblin: $distance blocks away!");
        setTrails(Collections.singletonList(Material.GOLD_NUGGET.toString()));
        setAnnouncementPriority(2);
        setPersistent(true);
        setUniqueLootList(new ArrayList<>(List.of(
                "berserker_charm.yml:0.2",
                "chameleon_charm.yml:0.2",
                "cheetah_charm.yml:0.2",
                "elephant_charm.yml:0.2",
                "firefly_charm.yml:0.2",
                "fishy_charm.yml:0.2",
                "lucky_charms.yml:0.2",
                "owl_charm.yml:0.2",
                "rabbit_charm.yml:0.2",
                "salamander_charm.yml:0.2",
                "scorpion_charm.yml:0.2",
                "shulker_charm.yml:0.2",
                "slowpoke_charm.yml:0.2",
                "vampiric_charm.yml:0.2")));
        setFollowDistance(100);
        setCustomModel("em_goblin_charms");
    }
}
