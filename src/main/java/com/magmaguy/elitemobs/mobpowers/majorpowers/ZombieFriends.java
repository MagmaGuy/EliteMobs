package com.magmaguy.elitemobs.mobpowers.majorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.ReinforcementMobEntity;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

        Zombie friend1 = (Zombie) eliteMobEntity.getLivingEntity().getWorld().spawnEntity(eliteMobEntity.getLivingEntity().getLocation(), EntityType.ZOMBIE);
        Zombie friend2 = (Zombie) eliteMobEntity.getLivingEntity().getWorld().spawnEntity(eliteMobEntity.getLivingEntity().getLocation(), EntityType.ZOMBIE);

        ReinforcementMobEntity reinforcement1 = new ReinforcementMobEntity(friend1,
                eliteMobEntity.getLevel(), ConfigValues.translationConfig.getString("ZombieFriends.Friend 1"));

        ReinforcementMobEntity reinforcement2 = new ReinforcementMobEntity(friend2,
                eliteMobEntity.getLevel(), ConfigValues.translationConfig.getString("ZombieFriends.Friend 2"));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!eliteMobEntity.getLivingEntity().isValid() || !friend1.isValid() && !friend2.isValid()) {

                    if (friend1.isValid()) {

                        nameClearer(reinforcement1);

                        friend1.setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieFriends.DeathMessage").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieFriends.DeathMessage")
                                        .size()))));

                    }

                    if (friend2.isValid()) {


                        nameClearer(reinforcement2);

                        friend2.setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieFriends.DeathMessage").
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

                    if (ThreadLocalRandom.current().nextDouble() < 0.5 && friend1.isValid()) {

                        nameClearer(reinforcement1);

                        friend1.setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieFriends.FriendDialog").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieFriends.FriendDialog")
                                        .size()))));

                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.5 && friend2.isValid()) {

                        nameClearer(reinforcement2);

                        friend2.setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieFriends.FriendDialog").
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
