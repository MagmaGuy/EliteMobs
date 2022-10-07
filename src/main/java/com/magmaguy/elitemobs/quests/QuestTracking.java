package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.customloottable.CustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.*;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.SpigotMessage;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
    private final List<Location> turnInNPCs = new ArrayList<>();
    private List<ObjectiveDestinations> objectiveDestinations = new ArrayList<>();
    private BukkitTask locationRefresher;
    private BukkitTask compassTask;
    private BossBar compassBar;
    private boolean questIsDone = false;

    public QuestTracking(Player player, CustomQuest customQuest) {
        this.player = player;
        this.customQuest = customQuest;
        startLocationGetter();
        startCompass();
        playerTrackingQuests.put(player, this);
        customQuest.getQuestObjectives().displayLazyObjectivesScoreboard(player);
    }

    public static boolean isTracking(Player player) {
        return playerTrackingQuests.containsKey(player);
    }

    public static void clear() {
        ((HashMap<Player, QuestTracking>) playerTrackingQuests.clone()).values().forEach(QuestTracking::stop);
    }

    public static void toggleTracking(Player player, String questID) {
        CustomQuest customQuest = (CustomQuest) PlayerData.getQuest(player.getUniqueId(), questID);
        if (customQuest == null) {
            player.sendMessage("[EliteMobs] Failed to get a valid quest with that quest ID!");
            return;
        }
        toggleTracking(player, customQuest);
    }

    public static void toggleTracking(Player player, CustomQuest quest) {
        if (playerTrackingQuests.containsKey(player))
            playerTrackingQuests.get(player).stop();
        else {
            if (quest == null) {
                player.sendMessage("[EliteMobs] Failed to get a valid quest with that quest ID!");
                return;
            }
            if (!quest.getCustomQuestsConfigFields().isTrackable()) return;
            new QuestTracking(player, quest);
        }
    }

    public void refreshScoreboard() {
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
        List<ObjectiveDestinations> destinations = new ArrayList<>();
        if (!quest.getQuestObjectives().isOver()) {
            questIsDone = false;
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (!objective.isObjectiveCompleted())
                    if (objective instanceof CustomKillObjective)
                        destinations.addAll(getKillLocations((CustomKillObjective) objective));
                    else if (objective instanceof DialogObjective)
                        destinations.addAll(getDialogLocations((DialogObjective) objective));
                    else if (objective instanceof CustomFetchObjective)
                        destinations.addAll(getFetchLocations((CustomFetchObjective) objective));
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> objectiveDestinations = destinations);
        } else {
            questIsDone = true;
            getTurnInNPC();
        }
    }

    private List<ObjectiveDestinations> getKillLocations(CustomKillObjective customKillObjective) {
        List<ObjectiveDestinations> destinations = new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        EntityTracker.getEliteMobEntities().values().forEach(eliteEntity -> {
            if (eliteEntity instanceof CustomBossEntity)
                if (((CustomBossEntity) eliteEntity).getPhaseBossEntity() != null &&
                        ((CustomBossEntity) eliteEntity).getPhaseBossEntity().getPhase1Config().getFilename().equals(customKillObjective.getCustomBossFilename())) {
                    locations.add(eliteEntity.getLocation());
                } else if (((CustomBossEntity) eliteEntity).getCustomBossesConfigFields().getFilename()
                        .equals(customKillObjective.getCustomBossFilename())) {
                    locations.add(eliteEntity.getLocation());
                }
        });
        destinations.add(new ObjectiveDestinations(customKillObjective, locations));
        return destinations;
    }

    private List<ObjectiveDestinations> getDialogLocations(DialogObjective dialogObjective) {
        List<ObjectiveDestinations> destinations = new ArrayList<>();
        EntityTracker.getNpcEntities().values().forEach(npcEntity -> {
            List<Location> locations = new ArrayList<>();
            if (npcEntity.getNPCsConfigFields().getFilename().equals(dialogObjective.getNpcFilename()))
                locations.add(npcEntity.getSpawnLocation());
            destinations.add(new ObjectiveDestinations(dialogObjective, locations));
        });
        return destinations;
    }

    private List<ObjectiveDestinations> getFetchLocations(CustomFetchObjective customFetchObjective) {
        List<ObjectiveDestinations> destinations = new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        EntityTracker.getEliteMobEntities().values().forEach(eliteEntity -> {
            if (eliteEntity instanceof CustomBossEntity)
                for (CustomLootEntry customLootEntry : ((CustomBossEntity) eliteEntity).getCustomBossesConfigFields().getCustomLootTable().getEntries())
                    if (customLootEntry instanceof EliteCustomLootEntry && ((EliteCustomLootEntry) customLootEntry).getFilename().equals(customFetchObjective.getKey()))
                        locations.add(eliteEntity.getLocation());
        });
        TreasureChest.getTreasureChestHashMap().values().forEach((treasureChest -> {
            for (CustomLootEntry customLootEntry : treasureChest.getCustomTreasureChestConfigFields().getCustomLootTable().getEntries())
                if (customLootEntry instanceof EliteCustomLootEntry && ((EliteCustomLootEntry) customLootEntry).getFilename().equals(customFetchObjective.getKey()))
                    locations.add(treasureChest.getLocation());
        }));
        destinations.add(new ObjectiveDestinations(customFetchObjective, locations));
        return destinations;
    }

    private void getTurnInNPC() {
        EntityTracker.getNpcEntities().values().forEach(npcEntity -> {
            if (npcEntity.getNPCsConfigFields().getFilename().equals(customQuest.getQuestTaker()))
                turnInNPCs.add(npcEntity.getSpawnLocation());
        });
    }

    public void stop() {
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
                if (!player.isOnline()) {
                    stop();
                    return;
                }
                updateCompassContents();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
    }

    private void updateCompassContents() {
        //for reference, character 32 is straight ahead
        String compassText = "---------------------------------------------------------------";
        List<LocationAndSymbol> locationAndSymbols = projectLocations();
        if (!locationAndSymbols.isEmpty())
            for (LocationAndSymbol pair : locationAndSymbols)
                compassText = compassText.substring(0, pair.getKey()) + pair.getValue() + compassText.substring(pair.getKey() + 1);
        else {
            World world = null;
            boolean locationsOutOfBounds = false;
            List<ObjectiveDestinations> tempDestinations = new ArrayList<>(objectiveDestinations);
            for (ObjectiveDestinations destinations : tempDestinations)
                for (Location location : destinations.getDestinations())
                    if (location != null && location.getWorld() != null) {
                        world = location.getWorld();
                        if (world.equals(player.getWorld())) {
                            locationsOutOfBounds = true;
                            break;
                        }
                    }

            if (!locationsOutOfBounds) {
                if (world != null) {
                    boolean wormholeIsViable = false;
                    for (Wormhole wormhole : Wormhole.getWormholes()) {
                        if (wormhole.getWormholeEntry1().getLocation() != null &&
                                wormhole.getWormholeEntry1().getLocation().getWorld() != null &&
                                wormhole.getWormholeEntry1().getLocation().getWorld().equals(player.getWorld())) {
                            if (wormhole.getWormholeEntry2().getLocation() != null &&
                                    wormhole.getWormholeEntry2().getLocation().getWorld() != null &&
                                    wormhole.getWormholeEntry2().getLocation().getWorld().equals(world)) {
                                LocationAndSymbol pair = processLocations(wormhole.getWormholeEntry1().getLocation(), null);
                                if (pair != null)
                                    compassText = compassText.substring(0, pair.getKey()) + pair.getValue() + compassText.substring(pair.getKey() + 1);
                                wormholeIsViable = true;
                            }

                        } else if (wormhole.getWormholeEntry2().getLocation() != null &&
                                wormhole.getWormholeEntry2().getLocation().getWorld() != null &&
                                wormhole.getWormholeEntry2().getLocation().getWorld().equals(player.getWorld())) {
                            if (wormhole.getWormholeEntry1().getLocation() != null &&
                                    wormhole.getWormholeEntry1().getLocation().getWorld() != null &&
                                    wormhole.getWormholeEntry1().getLocation().getWorld().equals(world)) {
                                LocationAndSymbol pair = processLocations(wormhole.getWormholeEntry2().getLocation(), null);
                                if (pair != null)
                                    compassText = compassText.substring(0, pair.getKey()) + pair.getValue() + compassText.substring(pair.getKey() + 1);
                                wormholeIsViable = true;
                            }
                        }
                    }
                    if (!wormholeIsViable)
                        compassText = QuestsConfig.getQuestDestinationInOtherWorld().replace("$world", world.getName());
                } else
                    compassText = QuestsConfig.getNoQuestDestinationFound();
            }
        }

        compassBar.setTitle(compassText);
        compassBar.addPlayer(player);
    }

    private List<LocationAndSymbol> projectLocations() {
        List<LocationAndSymbol> parsedVectors = new ArrayList<>();
        if (questIsDone) {
            for (Location location : new ArrayList<>(turnInNPCs)) {
                LocationAndSymbol locationAndSymbol = processLocations(location, null);
                if (locationAndSymbol != null) parsedVectors.add(locationAndSymbol);
            }
        } else
            for (ObjectiveDestinations iteratedObjectiveDestinations : new ArrayList<>(objectiveDestinations))
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
            if (Math.abs(angle) > Math.PI / 2D) return null;
            //Convert to degrees, each character has a resolution of 3 degrees
            return new LocationAndSymbol((int) (angle * 57D / 3D), getSymbol(objective));
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
        //case for portals
        if (objective == null) return "⬯";

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
            //if (event.getObjective().isObjectiveCompleted()) return;
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
            if (QuestsConfig.isAutoTrackQuestsOnAccept()) {
                toggleTracking(event.getPlayer(), (CustomQuest) event.getQuest());
                event.getPlayer().spigot().sendMessage(SpigotMessage.commandHoverMessage(
                        ChatColorConverter.convert(QuestsConfig.getChatTrackingMessage()),
                        ChatColorConverter.convert(QuestsConfig.getChatTrackingHover()),
                        QuestsConfig.getChatTrackingCommand().replace("$questID", event.getQuest().getQuestID().toString())
                ));
            } else
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
