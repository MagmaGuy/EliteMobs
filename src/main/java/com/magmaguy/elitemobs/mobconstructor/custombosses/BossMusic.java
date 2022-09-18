package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;


public class BossMusic {
    private final HashMap<Player, BukkitTask> players = new HashMap<>();
    @Getter
    private String name;
    @Getter
    private int durationTicks;
    @Getter
    private String name2 = null;
    @Getter
    private int durationTicks2 = -1;
    private BukkitTask bukkitTask = null;

    //Format: name=rsp.name:length=durations_ms->name=rsp.name:length=duration_ms
    public BossMusic(String rawString) {
        if (!rawString.contains("->")) {
            parse(rawString, 1);
        } else {
            String[] rawEntries = rawString.split("->");
            parse(rawEntries[0], 1);
            parse(rawEntries[1], 2);
        }
    }

    private void parse(String rawString, int entryNumber) {
        String[] strings = rawString.split(" ");
        for (String string : strings) {
            String[] parsed = string.split("=");
            switch (parsed[0]) {
                case "name":
                    if (entryNumber == 1) name = parsed[1];
                    else name2 = parsed[1];
                    break;
                case "length":
                    if (entryNumber == 1) {
                        durationTicks = (int) (Integer.parseInt(parsed[1]) / 1000D * 20D);
                    } else {
                        durationTicks2 = (int) (Integer.parseInt(parsed[1]) / 1000D * 20D);
                    }
                    break;
                default:
                    new WarningMessage("Failed to get value for boss music!");
            }
        }
    }

    public void start(CustomBossEntity customBossEntity) {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!customBossEntity.isValid()) {
                    stop();
                    return;
                }
                play(customBossEntity.getLocation(), customBossEntity.getCustomBossesConfigFields().getFollowDistance());
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    public void stop() {
        if (bukkitTask != null)
            bukkitTask.cancel();
        for (Map.Entry<Player, BukkitTask> entry : players.entrySet()) {
            entry.getKey().stopSound(name);
            entry.getValue().cancel();
        }
    }

    private void play(Location location, double range) {
        location.getWorld()
                .getNearbyEntities(
                        location,
                        range,
                        range,
                        range,
                        entity -> entity.getType().equals(EntityType.PLAYER))
                .forEach(player -> {
                    if (!players.containsKey((Player) player)) {
                        ((Player) player).playSound(player.getLocation(), name, 1f, 1f);
                        startLoopingTask((Player) player, durationTicks);
                    }
                });
    }

    private void startLoopingTask(Player player, int durationTicks) {
        BukkitTask songTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (name2 != null)
                    player.playSound(player.getLocation(), name2, 1f, 1f);
                else
                    player.playSound(player.getLocation(), name, 1f, 1f);
                int duration;
                if (durationTicks2 > 0) duration = durationTicks2;
                else duration = durationTicks;
                startLoopingTask(player, duration);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, durationTicks);
        players.put(player, songTask);
    }
}
