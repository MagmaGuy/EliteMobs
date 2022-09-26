package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.premade.ZombieParentsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.meta.MajorPower;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by MagmaGuy on 13/05/2017.
 */
public class ZombieParents extends MajorPower implements Listener {

    public ZombieParents() {
        super(PowersConfig.getPower("zombie_parents.yml"));
    }

    private static void startDialog(CustomBossEntity reinforcementMom, CustomBossEntity reinforcementDad, EliteEntity bossEntity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bossEntity.isValid()) {
                    doDeathMessages(reinforcementDad, reinforcementMom);
                    cancel();
                } else {
                    doDialog(reinforcementDad, reinforcementMom, bossEntity);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20L * 8);
    }

    private static void doDeathMessages(CustomBossEntity reinforcementDad, CustomBossEntity reinforcementMom) {
        if (reinforcementDad.isValid()) {
            nameClearer(reinforcementDad);
            reinforcementDad.getLivingEntity().setCustomName(ZombieParentsConfig.getDeathMessage().
                    get(ThreadLocalRandom.current().nextInt(ZombieParentsConfig.getDeathMessage().size())));
        }

        if (reinforcementMom.isValid()) {
            nameClearer(reinforcementMom);
            reinforcementMom.getLivingEntity().setCustomName(ZombieParentsConfig.getDeathMessage().
                    get(ThreadLocalRandom.current().nextInt(ZombieParentsConfig.getDeathMessage().size())));
        }
    }

    private static void doDialog(CustomBossEntity reinforcementDad, CustomBossEntity reinforcementMom, EliteEntity bossEntity) {
        if (ThreadLocalRandom.current().nextDouble() < 0.5) {
            nameClearer(bossEntity);
            bossEntity.getLivingEntity().setCustomName(ZombieParentsConfig.getBossEntityDialog().
                    get(ThreadLocalRandom.current().nextInt(ZombieParentsConfig.getBossEntityDialog().size())));
        }

        if (ThreadLocalRandom.current().nextDouble() < 0.5 && reinforcementDad.isValid()) {
            nameClearer(reinforcementDad);
            reinforcementDad.getLivingEntity().setCustomName(ZombieParentsConfig.getZombieDad().
                    get(ThreadLocalRandom.current().nextInt(ZombieParentsConfig.getZombieDad().size())));
        }

        if (ThreadLocalRandom.current().nextDouble() < 0.5 && reinforcementMom.isValid()) {
            nameClearer(reinforcementMom);
            reinforcementMom.getLivingEntity().setCustomName(ZombieParentsConfig.getZombieMom().
                    get(ThreadLocalRandom.current().nextInt(ZombieParentsConfig.getZombieMom().size())));
        }
    }

    private static void nameClearer(EliteEntity eliteEntity) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (eliteEntity.isValid())
                    eliteEntity.setName(eliteEntity.getName(), true);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20L * 3);

    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        ZombieParents zombieParents = (ZombieParents) event.getEliteMobEntity().getPower(this);
        if (zombieParents == null) return;
        if (zombieParents.isFiring()) return;

        if (ThreadLocalRandom.current().nextDouble() > 0.01) return;
        zombieParents.setFiring(false);

        CustomBossEntity reinforcementMom = CustomBossEntity.createCustomBossEntity("zombie_parents_mom.yml");
        try {
            reinforcementMom.spawn(event.getEntity().getLocation(), event.getEliteMobEntity().getLevel(), false);
        } catch (Exception ex) {
            new WarningMessage("Failed to spawn Zombie Parents Mom reinforcement!");
            return;
        }
        CustomBossEntity reinforcementDad = CustomBossEntity.createCustomBossEntity("zombie_parents_dad.yml");
        try {
            reinforcementDad.spawn(event.getEntity().getLocation(), event.getEliteMobEntity().getLevel(), false);
        } catch (Exception ex) {
            new WarningMessage("Failed to spawn Zombie Parents Dad reinforcement!");
            return;
        }

        startDialog(reinforcementMom, reinforcementDad, event.getEliteMobEntity());
    }

}
