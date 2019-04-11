package com.magmaguy.elitemobs.mobpowers.majorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.ReinforcementMobEntity;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.ChatColorConverter.convert;

/**
 * Created by MagmaGuy on 18/05/2017.
 */
public class ZombieFriends extends MajorPower implements Listener {

    public static HashSet<EliteMobEntity> activatedZombies = new HashSet<>();

    @Override
    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void onHit(EntityDamageEvent event) {

        EliteMobEntity eliteMobEntity = EventValidator.getEventEliteMob(this, event);
        if (eliteMobEntity == null) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.01) return;
        if (activatedZombies.contains(eliteMobEntity)) return;
        activatedZombies.add(eliteMobEntity);

        ReinforcementMobEntity reinforcement1 = new ReinforcementMobEntity(EntityType.ZOMBIE, eliteMobEntity.getLivingEntity().getLocation(),
                eliteMobEntity.getLevel(), ConfigValues.translationConfig.getString("ZombieFriends.Friend 1"), CreatureSpawnEvent.SpawnReason.CUSTOM);

        ReinforcementMobEntity reinforcement2 = new ReinforcementMobEntity(EntityType.ZOMBIE, eliteMobEntity.getLivingEntity().getLocation(),
                eliteMobEntity.getLevel(), ConfigValues.translationConfig.getString("ZombieFriends.Friend 2"), CreatureSpawnEvent.SpawnReason.CUSTOM);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!eliteMobEntity.getLivingEntity().isValid() || !reinforcement1.getLivingEntity().isValid() && !reinforcement2.getLivingEntity().isValid()) {

                    if (reinforcement1.getLivingEntity().isValid()) {

                        nameClearer(reinforcement1);

                        reinforcement1.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieFriends.DeathMessage").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieFriends.DeathMessage")
                                        .size()))));

                    }

                    if (reinforcement2.getLivingEntity().isValid()) {


                        nameClearer(reinforcement2);

                        reinforcement2.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieFriends.DeathMessage").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieFriends.DeathMessage")
                                        .size()))));

                    }

                    cancel();
                    return;

                } else {

                    if (ThreadLocalRandom.current().nextDouble() < 0.5) {

                        nameClearer(eliteMobEntity);

                        eliteMobEntity.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieFriends.ZombieDialog").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieFriends.ZombieDialog")
                                        .size()))));

                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.5 && reinforcement1.getLivingEntity().isValid()) {

                        nameClearer(reinforcement1);

                        reinforcement1.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieFriends.FriendDialog").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieFriends.FriendDialog")
                                        .size()))));

                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.5 && reinforcement2.getLivingEntity().isValid()) {

                        nameClearer(reinforcement2);

                        reinforcement2.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieFriends.FriendDialog").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieFriends.FriendDialog")
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
