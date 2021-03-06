package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CustomBossBossBar {

    private final CustomBossEntity customBossEntity;
    public BossBar bossBar;
    public BukkitTask bukkitTask;
    public Player player;

    public CustomBossBossBar(CustomBossEntity customBossEntity,
                             Player player,
                             boolean persistentTracking) {
        this.customBossEntity = customBossEntity;
        this.player = player;
        if (persistentTracking)
            customBossEntity.trackingPlayer.add(player);
        startBossBarTask();
    }

    public void startBossBarTask() {

        String locationString = (int) customBossEntity.getLocation().getX() +
                ", " + (int) customBossEntity.getLocation().getY() +
                ", " + (int) customBossEntity.getLocation().getZ();
        bossBar = Bukkit.createBossBar(customBossEntity.bossBarMessage(player, locationString), BarColor.GREEN, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);

        if (customBossEntity.getHealth() / customBossEntity.getMaxHealth() > 1 || customBossEntity.getHealth() / customBossEntity.getMaxHealth() < 0){
            new WarningMessage("The following boss had more health than it should: " + customBossEntity.getName());
            new WarningMessage("This is a problem usually caused by running more than one plugin that modifies mob health!" +
                    " EliteMobs can't fix this issue because it is being caused by another plugin." +
                    " If you want EliteMobs to work correctly, find a way to fix this issue with whatever other plugin is causing it.");
            return;
        }

        bossBar.setProgress(customBossEntity.getHealth() / customBossEntity.getMaxHealth());
        bossBar.addPlayer(player);

        customBossEntity.playerBossBars.put(player, bossBar);
        customBossEntity.customBossBossBars.add(this);

        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (customBossEntity.getLivingEntity() == null && customBossEntity.simplePersistentEntity == null
                        || !player.isOnline() ||
                        !player.getWorld().equals(customBossEntity.getLocation().getWorld()) ||
                        !customBossEntity.trackingPlayer.contains(player) &&
                                player.getLocation().distance(customBossEntity.getLocation()) > 20 ||
                        customBossEntity.getHealth() <= 0) {
                    remove(false);
                    return;
                }

                String locationString = (int) customBossEntity.getLocation().getX() +
                        ", " + (int) customBossEntity.getLocation().getY() +
                        ", " + (int) customBossEntity.getLocation().getZ();
                bossBar.setTitle(customBossEntity.bossBarMessage(player, locationString));
                bossBar.setProgress(customBossEntity.getHealth() / customBossEntity.getMaxHealth());
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);
    }

    public void remove(boolean isFullRemove) {
        bossBar.removeAll();
        bukkitTask.cancel();
        customBossEntity.playerBossBars.remove(player);
        if (!isFullRemove)
            customBossEntity.customBossBossBars.remove(this);
    }

    public static class CustomBossBossBarEvent implements Listener {
        @EventHandler
        public void displayBossBar(EliteMobEnterCombatEvent event) {
            if (event.getEliteMobEntity().customBossEntity == null) return;
            CustomBossEntity customBossEntity = event.getEliteMobEntity().customBossEntity;
            if (customBossEntity.customBossConfigFields.getLocationMessage() == null ||
                    customBossEntity.customBossConfigFields.getLocationMessage().length() == 0)
                return;
            customBossEntity.startBossBarLocalScan();
        }
    }

}
