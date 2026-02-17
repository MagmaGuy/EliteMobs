package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZombieKingBoss extends CustomBossesConfigFields {
    public ZombieKingBoss() {
        super("zombie_king",
                EntityType.ZOMBIE,
                true,
                "$eventBossLevel &4Zombie King",
                "dynamic");
        setPersistent(true);
        setHealthMultiplier(4);
        setDamageMultiplier(1.25);
        setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        setMainHand(new ItemStack(Material.GOLDEN_AXE));
        setPowers(new ArrayList<>(List.of("flame_pyre.yml", "flamethrower.yml", "summon_the_returned.yml", "spirit_walk.yml",
                "summonable:summonType=GLOBAL:filename=the_living_dead.yml:amount=5:customSpawn=normal_surface_spawn.yml")));
        setSpawnMessage("&cThe Zombie King has been sighted!");
        setDeathMessage("&aThe Zombie King has been slain by $players!");
        majorBossDeathString("The Zombie King has been slain!");
        setEscapeMessage("&4The Zombie King has escaped!");
        setLocationMessage("&cZombie King: $distance blocks away!");
        setUniqueLootList(Collections.singletonList("zombie_kings_axe.yml"));
        setTrails(Collections.singletonList(Particle.FLAME.toString()));
        setAnnouncementPriority(2);
    }
}
