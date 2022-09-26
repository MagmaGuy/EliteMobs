package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.premade.ZombieFriendsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.meta.MajorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by MagmaGuy on 18/05/2017.
 */
public class ZombieFriends extends MajorPower implements Listener {

    public ZombieFriends() {
        super(PowersConfig.getPower("zombie_friends.yml"));
    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        ZombieFriends zombieFriendConfig = (ZombieFriends) event.getEliteMobEntity().getPower(this);
        if (zombieFriendConfig == null) return;
        if (zombieFriendConfig.isFiring()) return;

        if (ThreadLocalRandom.current().nextDouble() > 0.01) return;
        zombieFriendConfig.setFiring(true);

        CustomBossEntity reinforcement1 = CustomBossEntity.createCustomBossEntity("zombie_friends_friend.yml");
        reinforcement1.spawn(event.getEntity().getLocation(), event.getEliteMobEntity().getLevel(), false);
        CustomBossEntity reinforcement2 = CustomBossEntity.createCustomBossEntity("zombie_friends_friend.yml");
        reinforcement2.spawn(event.getEntity().getLocation(), event.getEliteMobEntity().getLevel(), false);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!event.getEliteMobEntity().isValid() || !reinforcement1.isValid() && !reinforcement2.isValid()) {

                    if (reinforcement1 != null && reinforcement1.isValid()) {
                        nameClearer(reinforcement1);
                        reinforcement1.getLivingEntity().setCustomName(ChatColorConverter.convert(ZombieFriendsConfig.friendDeathMessage
                                .get(ThreadLocalRandom.current().nextInt(ZombieFriendsConfig.friendDeathMessage.size()))));
                    }

                    if (reinforcement2 != null && reinforcement2.isValid()) {
                        nameClearer(reinforcement2);
                        reinforcement2.getLivingEntity().setCustomName(ChatColorConverter.convert(ZombieFriendsConfig.friendDeathMessage
                                .get(ThreadLocalRandom.current().nextInt(ZombieFriendsConfig.friendDeathMessage.size()))));
                    }

                    cancel();

                } else {

                    if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                        nameClearer(event.getEliteMobEntity());
                        event.getEliteMobEntity().getLivingEntity().setCustomName(ChatColorConverter.convert(ZombieFriendsConfig.originalEntityDialog
                                .get(ThreadLocalRandom.current().nextInt(ZombieFriendsConfig.originalEntityDialog.size()))));
                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.5 && reinforcement1.isValid()) {
                        nameClearer(reinforcement1);
                        reinforcement1.getLivingEntity().setCustomName(ChatColorConverter.convert(ZombieFriendsConfig.reinforcementDialog
                                .get(ThreadLocalRandom.current().nextInt(ZombieFriendsConfig.reinforcementDialog.size()))));
                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.5 && reinforcement2.isValid()) {
                        nameClearer(reinforcement2);
                        reinforcement2.getLivingEntity().setCustomName(ChatColorConverter.convert(ZombieFriendsConfig.reinforcementDialog
                                .get(ThreadLocalRandom.current().nextInt(ZombieFriendsConfig.reinforcementDialog.size()))));
                    }

                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20 * 8);

    }

    private void nameClearer(EliteEntity eliteEntity) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (eliteEntity.isValid())
                    eliteEntity.setName(eliteEntity.getName(), true);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);

    }

}
