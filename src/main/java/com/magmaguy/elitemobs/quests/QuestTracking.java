package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.*;
import com.magmaguy.elitemobs.utils.SpigotMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestTracking {

    @Getter
    private static final HashMap<Player, QuestTracking> playerTrackingQuests = new HashMap<>();
    private final Player player;
    @Getter
    private final CustomQuest customQuest;
    private List<Location> turnInNPCs = new ArrayList<>();
    private List<ObjectiveDestinations> objectiveDestinations = new ArrayList<>();
    private BukkitTask locationRefresher;
    private BukkitTask compassTask;
    private BossBar compassBar;
    private Scoreboard scoreboard;
    private boolean questIsDone = false;
    public QuestTracking(Player player, CustomQuest customQuest) {
        this.player = player;
        this.customQuest = customQuest;
        startLocationGetter();
        startCompass();
        playerTrackingQuests.put(player, this);
        scoreboard = customQuest.getQuestObjectives().displayLazyObjectivesScoreboard(player);
    }

    public static boolean isTracking(Player player) {
        return playerTrackingQuests.containsKey(player);
    }

    public static void clear() {
        ((HashMap<Player, QuestTracking>) playerTrackingQuests.clone()).values().forEach(QuestTracking::stop);
    }

    public static void toggleTracking(Player player, String questID) {
        if (playerTrackingQuests.containsKey(player))
            playerTrackingQuests.get(player).stop();
        else {
            CustomQuest customQuest = (CustomQuest) PlayerData.getQuest(player.getUniqueId(), questID);
            if (customQuest == null) {
                player.sendMessage("[EliteMobs] Failed to get a valid quest with that quest ID!");
                return;
            }
            if (!customQuest.getCustomQuestsConfigFields().isTrackable()) return;
            new QuestTracking(player, customQuest);
        }
    }

    public void refreshScoreboard(){
        customQuest.getQuestObjectives().displayLazyObjectivesScoreboard(player);
    }

    private void startLocationGetter() {
        locationRefresher = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isValid()) {
                    stop();
                    return;
                }
                updateLocations(customQuest);
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 0L, 20L * 60L);
    }

    public void updateLocations(Quest quest) {
        objectiveDestinations.clear();
        if (!quest.getQuestObjectives().isOver()) {
            questIsDone = false;
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (!objective.isObjectiveCompleted())
                    if (objective instanceof CustomKillObjective)
                        getKillLocations((CustomKillObjective) objective);
                    else if (objective instanceof DialogObjective)
                        getDialogLocations((DialogObjective) objective);
                    else if (objective instanceof CustomFetchObjective)
                        getFetchLocations((CustomFetchObjective) objective);
        } else {
            questIsDone = true;
            getTurnInNPC();
        }
    }

    private void getKillLocations(CustomKillObjective customKillObjective) {
        EntityTracker.getEliteMobs().values().forEach(eliteEntity -> {
            List<Location> locations = new ArrayList<>();
            if (eliteEntity instanceof CustomBossEntity &&
                    ((CustomBossEntity) eliteEntity).getCustomBossesConfigFields().getFilename()
                            .equals(customKillObjective.getEntityName()))
                locations.add(eliteEntity.getLocation());
            new ObjectiveDestinations(customKillObjective, locations);
        });
    }

    private void getDialogLocations(DialogObjective dialogObjective) {
        EntityTracker.getNPCEntities().values().forEach(npcEntity -> {
            List<Location> locations = new ArrayList<>();
            if (npcEntity.getNpCsConfigFields().getFilename().equals(dialogObjective.getNpcFilename()))
                locations.add(npcEntity.getSpawnLocation());
            new ObjectiveDestinations(dialogObjective, locations);
        });
    }

    private void getFetchLocations(CustomFetchObjective customFetchObjective) {
        EntityTracker.getEliteMobs().values().forEach(eliteEntity -> {
            List<Location> locations = new ArrayList<>();
            if (eliteEntity instanceof CustomBossEntity)
                for (String loot : ((CustomBossEntity) eliteEntity).getCustomBossesConfigFields().getUniqueLootList())
                    if (loot.contains(customFetchObjective.getKey()))
                        locations.add(eliteEntity.getLocation());
            new ObjectiveDestinations(customFetchObjective, locations);
        });
    }

    private void getTurnInNPC() {
        EntityTracker.getNPCEntities().values().forEach(npcEntity -> {
            if (npcEntity.getNpCsConfigFields().getFilename().equals(customQuest.getQuestTaker()))
                turnInNPCs.add(npcEntity.getSpawnLocation());
        });
    }

    private void stop() {
        playerTrackingQuests.remove(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        locationRefresher.cancel();
        compassTask.cancel();
        compassBar.removeAll();
    }

    private void startCompass() {
        compassBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        compassTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isValid()) {
                    stop();
                    return;
                }
                updateCompassContents();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 10L);
    }

    private void updateCompassContents() {
        //for reference, character 32 is straight ahead
        String compassText = "---------------------------------------------------------------";
        for (LocationAndSymbol pair : projectLocations()) {
            compassText = compassText.substring(0, pair.getKey()) + pair.getValue() + compassText.substring(pair.getKey() + 1);
        }
        compassBar.setTitle(compassText);
        compassBar.addPlayer(player);
    }

    private List<LocationAndSymbol> projectLocations() {
        List<LocationAndSymbol> parsedVectors = new ArrayList<>();
        if (questIsDone) {
            for (Location location : turnInNPCs) {
                LocationAndSymbol locationAndSymbol = processLocations(location, null);
                if (locationAndSymbol != null) parsedVectors.add(locationAndSymbol);
            }
        } else
            for (ObjectiveDestinations iteratedObjectiveDestinations : objectiveDestinations)
                for (Location location : iteratedObjectiveDestinations.getDestinations()) {
                    LocationAndSymbol locationAndSymbol = processLocations(location, iteratedObjectiveDestinations.getObjective());
                    if (locationAndSymbol != null) parsedVectors.add(locationAndSymbol);
                }
        return parsedVectors;
    }

    private LocationAndSymbol processLocations(Location location, Objective objective) {
        if (player.getWorld().equals(location.getWorld())) {
            Vector toTarget = toTargetVector(player, location);
            double angle = getAngle(toTarget, player);
            if (angle > Math.abs(Math.PI / 2D)) return null;
            //Convert to degrees, each character has a resolution of 6 degrees
            return new LocationAndSymbol((int) (angle * 57D / 6d), getSymbol(objective));
        }
        return null;
    }

    private Vector toTargetVector(Player player, Location location) {
        return location.clone().add(new Vector(0, 1.85, 0)).subtract(player.getEyeLocation()).toVector().normalize();
    }

    private double getAngle(Vector toTarget, Player player) {
        double angle = player.getEyeLocation().getDirection().setY(0).angle(toTarget.clone().setY(0));
        if (toTarget.getX() * player.getEyeLocation().getDirection().getZ() - toTarget.getZ() * player.getEyeLocation().getDirection().getX() > 0)
            angle *= -1;
        return angle;
    }

    private String getSymbol(Objective objective) {
        if (questIsDone)
            return "⦿";
        if (objective instanceof KillObjective)
            return "☠";
        else if (objective instanceof DialogObjective)
            return "⦿";
        else if (objective instanceof CustomFetchObjective)
            return "⚔";
        return "⦿";
    }

    public static class QuestTrackingEvents implements Listener {
        @EventHandler
        public void onPlayerLogout(PlayerQuitEvent event) {
            QuestTracking questTracking = getPlayerTrackingQuests().get(event.getPlayer());
            if (questTracking == null) return;
            questTracking.stop();
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestProgressEvent(QuestProgressionEvent event) {
            if (!event.getObjective().isObjectiveCompleted()) return;
            if (!isTracking(event.getPlayer())) return;
            if (!getPlayerTrackingQuests().get(event.getPlayer()).getCustomQuest().getQuestID().equals(event.getQuest().getQuestID()))
                return;
            getPlayerTrackingQuests().get(event.getPlayer())
                    .updateLocations(getPlayerTrackingQuests().get(event.getPlayer()).getCustomQuest());
            getPlayerTrackingQuests().get(event.getPlayer()).refreshScoreboard();
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestCompleteEvent(QuestCompleteEvent event) {
            if (!isTracking(event.getPlayer())) return;
            if (!getPlayerTrackingQuests().get(event.getPlayer()).getCustomQuest().getQuestID().equals(event.getQuest().getQuestID()))
                return;
            getPlayerTrackingQuests().get(event.getPlayer()).stop();
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onQuestAcceptEvent(QuestAcceptEvent event) {
            if (!(event.getQuest() instanceof CustomQuest)) return;
            if (!((CustomQuest) event.getQuest()).getCustomQuestsConfigFields().isTrackable()) return;
            event.getPlayer().spigot().sendMessage(SpigotMessage.commandHoverMessage(
                    ChatColorConverter.convert(QuestsConfig.getChatTrackMessage()),
                    ChatColorConverter.convert(QuestsConfig.getChatTrackHover()),
                    QuestsConfig.getChatTrackCommand().replace("$questID", event.getQuest().getQuestID().toString())
            ));
        }

    }

    private class ObjectiveDestinations {
        @Getter
        private final Objective objective;
        @Getter
        private final List<Location> destinations;

        private ObjectiveDestinations(Objective objective, List<Location> destinations) {
            this.objective = objective;
            this.destinations = destinations;
            objectiveDestinations.add(this);
        }
    }

    private class LocationAndSymbol {
        @Getter
        private final int key;
        @Getter
        private final String value;

        private LocationAndSymbol(int key, String value) {
            //adjust the key to the center of the compass display
            this.key = key + 31;
            this.value = value;
        }
    }

}
