package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CustomBossBossBar {

    private final CustomBossEntity customBossEntity;
    private final Map<Player, BossBar> bossBars = new HashMap<>();
    private BukkitTask bossBarUpdater;
    private HashSet<Player> trackingPlayers = new HashSet<>();

    public CustomBossBossBar(CustomBossEntity customBossEntity) {
        this.customBossEntity = customBossEntity;
        start();
        sendLocation();
    }

    private void sendLocation() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(customBossEntity.getLocation().getWorld())) continue;
            TextComponent interactiveMessage = new TextComponent(MobCombatSettingsConfig.bossLocationMessage);
            interactiveMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs trackcustomboss " + customBossEntity.getEliteUUID()));
            interactiveMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Track the " + customBossEntity.getName()).create()));
            player.spigot().sendMessage(interactiveMessage);
        }
    }

    public void addTrackingPlayer(Player player) {
        if (!trackingPlayers.contains(player))
            trackingPlayers.add(player);
        else
            trackingPlayers.remove(player);
    }

    public void removeTrackingPlayer(Player player) {
        trackingPlayers.remove(player);
        BossBar bossBar = bossBars.remove(player);
        if (bossBar != null) bossBar.removeAll();
    }

    public void remove() {
        bossBars.entrySet().removeIf(entry -> {
            entry.getValue().removeAll();
            return true;
        });
        trackingPlayers.clear();
        bossBarUpdater.cancel();
    }

    /**
     * Used not only for generating the boss bar message, but also the message that appears on the tracking screen for players
     *
     * @param player
     * @param locationString
     * @return
     */
    public String bossBarMessage(Player player, String locationString) {
        if (customBossEntity.getCustomBossesConfigFields().getLocationMessage().contains("$distance") ||
                customBossEntity.getCustomBossesConfigFields().getLocationMessage().contains("$location")) {
            if (!player.getLocation().getWorld().equals(customBossEntity.getLocation().getWorld()))
                return ChatColorConverter.convert(MobCombatSettingsConfig.defaultOtherWorldBossLocationMessage
                        .replace("$name", customBossEntity.getName()));

            return ChatColorConverter.convert(customBossEntity.getCustomBossesConfigFields().getLocationMessage()
                            .replace("$distance", "" + (int) customBossEntity.getLocation().distance(player.getLocation())))
                    .replace("$location", locationString);
        }

        return ChatColorConverter.convert(customBossEntity.getCustomBossesConfigFields().getLocationMessage());

    }

    private void updateBossBar(Player player, BossBar bossBar) {
        String locationString = customBossEntity.getLocation().getBlockX() +
                ", " + customBossEntity.getLocation().getBlockY() +
                ", " + customBossEntity.getLocation().getBlockZ();
        bossBar.setTitle(bossBarMessage(player, locationString));
        if (customBossEntity.getHealth() / customBossEntity.getMaxHealth() > 1) return;
        bossBar.setProgress(customBossEntity.getHealth() / customBossEntity.getMaxHealth());
    }

    public void start() {
        bossBarUpdater = new BukkitRunnable() {
            @Override
            public void run() {
                //This can happen on phase changes where boss bars might not be configured on subsequent entities
                if (customBossEntity.getCustomBossesConfigFields().getLocationMessage() == null) {
                    cancel();
                    return;
                }

                HashSet<Player> freshIteration = new HashSet<>();
                for (Player player : trackingPlayers) {
                    freshIteration.add(player);
                    if (!bossBars.containsKey(player)) createBossBar(player);
                    updateBossBar(player, bossBars.get(player));
                }

                //Remove players that have stopped
                bossBars.entrySet().removeIf((entry) -> {
                    if (!trackingPlayers.contains(entry.getKey())) {
                        entry.getValue().removeAll();
                        return true;
                    }
                    return false;
                });

                //nearby player check
                if (customBossEntity.isValid())
                    for (Entity entity : customBossEntity.getLivingEntity().getNearbyEntities(30, 30, 30))
                        if (entity.getType().equals(EntityType.PLAYER)) {
                            if (!freshIteration.contains((Player) entity))
                                createBossBar((Player) entity);
                            updateBossBar((Player) entity, bossBars.get((Player) entity));
                        }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);
    }

    private void createBossBar(Player player) {
        String locationString = (int) customBossEntity.getLocation().getX() +
                ", " + (int) customBossEntity.getLocation().getY() +
                ", " + (int) customBossEntity.getLocation().getZ();
        BossBar bossBar = Bukkit.createBossBar(bossBarMessage(player, locationString), BarColor.GREEN, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        bossBar.addPlayer(player);

        if (customBossEntity.getHealth() / customBossEntity.getMaxHealth() > 1 || customBossEntity.getHealth() / customBossEntity.getMaxHealth() < 0) {
            new WarningMessage("The following boss had more health than it should: " + customBossEntity.getName());
            new WarningMessage("This is a problem usually caused by running more than one plugin that modifies mob health!" +
                    " EliteMobs can't fix this issue because it is being caused by another plugin." +
                    " If you want EliteMobs to work correctly, find a way to fix this issue with whatever other plugin is causing it.");
            return;
        }

        bossBars.put(player, bossBar);
    }

}
