package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AlphaWerewolfP1Boss extends CustomBossesConfigFields {
    public AlphaWerewolfP1Boss() {
        super("alpha_werewolf_p1",
                EntityType.VINDICATOR,
                true,
                "$eventBossLevel &7Enraged Vindicator",
                "dynamic");
        setPersistent(true);
        setHealthMultiplier(4);
        setDamageMultiplier(2);
        setPhases(List.of("alpha_werewolf_p2.yml:0.99"));
        setPowers(new ArrayList<>(new ArrayList<>(List.of("spirit_walk.yml",
                "summonable:summonType=GLOBAL:filename=gamma_werewolf.yml:amount=1:customSpawn=normal_surface_spawn.yml",
                "summonable:summonType=GLOBAL:filename=omega_wolf.yml:amount=2:customSpawn=normal_surface_spawn.yml"))));
        setSpawnMessage("&cThe howls of an Alpha Werewolf are heard!");
        setDeathMessage("&aThe Alpha Werewolf has been stopped by $players!");
        setDeathMessages(new ArrayList<>(List.of(
                "&e&l---------------------------------------------",
                "&4The Alpha Wolf has been put down!",
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------")));
        setEscapeMessage("&4Dawn breaks, the Alpha Wolf vanishes without a trace!");
        setLocationMessage("&7Enraged Vindicator: $distance blocks away!");
        setUniqueLootList(new ArrayList<>(List.of(
                "werewolf_bone.yml:0.2",
                "werewolf_bone.yml:0.2",
                "werewolf_bone.yml:0.2",
                "werewolf_bone.yml:0.2",
                "werewolf_bone.yml:0.2",
                "wolfsbane.yml:0.2")));
        setTrails(Collections.singletonList(Material.BONE.toString()));
        setAnnouncementPriority(2);
        HashMap<Material, Double> damageModifiers = new HashMap<>();
        damageModifiers.put(Material.IRON_SWORD, 2D);
        damageModifiers.put(Material.IRON_AXE, 2D);
        setDamageModifiers(damageModifiers);
        setFollowDistance(100);
    }
}

