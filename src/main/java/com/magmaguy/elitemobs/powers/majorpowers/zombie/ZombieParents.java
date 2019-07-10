package com.magmaguy.elitemobs.powers.majorpowers.zombie;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.MajorPower;
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

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        ZombieParents zombieParents = (ZombieParents) event.getEliteMobEntity().getPower(this);
        if (zombieParents == null) return;
        if (zombieParents.getIsFiring()) return;

        if (ThreadLocalRandom.current().nextDouble() > 0.01) return;
        zombieParents.setIsFiring(false);

        CustomBossEntity reinforcementMom = CustomBossEntity.constructCustomBoss("zombie_parents_mom.yml", event.getEntity().getLocation(), event.getEliteMobEntity().getLevel());
        CustomBossEntity reinforcementDad = CustomBossEntity.constructCustomBoss("zombie_parents_dad.yml", event.getEntity().getLocation(), event.getEliteMobEntity().getLevel());

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!event.getEliteMobEntity().getLivingEntity().isValid()) {

                    if (reinforcementDad.getLivingEntity().isValid()) {

                        nameClearer(reinforcementDad);

                        reinforcementDad.getLivingEntity().setCustomName(ChatColorConverter.convert(ConfigValues.translationConfig.getStringList("ZombieParents.DeathMessage").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieParents.DeathMessage")
                                        .size()))));

                    }

                    if (reinforcementMom.getLivingEntity().isValid()) {

                        nameClearer(reinforcementMom);

                        reinforcementMom.getLivingEntity().setCustomName(ChatColorConverter.convert(ConfigValues.translationConfig.getStringList("ZombieParents.DeathMessage").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieParents.DeathMessage")
                                        .size()))));

                    }

                    cancel();

                } else {

                    if (ThreadLocalRandom.current().nextDouble() < 0.5) {

                        nameClearer(event.getEliteMobEntity());

                        event.getEliteMobEntity().getLivingEntity().setCustomName(ChatColorConverter.convert(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieDialog").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieDialog")
                                        .size()))));

                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.5 && reinforcementDad.getLivingEntity().isValid()) {

                        nameClearer(reinforcementDad);

                        reinforcementDad.getLivingEntity().setCustomName(ChatColorConverter.convert(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieDadDialog").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieDadDialog")
                                        .size()))));

                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.5 && reinforcementMom.getLivingEntity().isValid()) {

                        nameClearer(reinforcementMom);

                        reinforcementMom.getLivingEntity().setCustomName(ChatColorConverter.convert(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieMomDialog").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieMomDialog")
                                        .size()))));

                    }

                }

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20 * 8);

    }

    private void nameClearer(EliteMobEntity eliteMobEntity) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (eliteMobEntity.getLivingEntity().isValid())
                    eliteMobEntity.setName(eliteMobEntity.getName());
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);

    }

}
