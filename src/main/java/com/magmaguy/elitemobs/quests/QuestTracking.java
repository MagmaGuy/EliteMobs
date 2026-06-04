package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.customloottable.CustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.elitemobs.quests.objectives.*;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.wormhole.WormholeNavigation;
import com.magmaguy.magmacore.util.SpigotMessage;
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
import java.util.UUID;

public class QuestTracking {

    @Getter
    private static final HashMap<UUID, QuestTracking> playerTrackingQuests = new HashMap<>();
    private final Player player;
    @Getter
    private final CustomQuest customQuest;
    private final List<Location> turnInNPCs = new ArrayList<>();
    private List<ObjectiveDestinations> objectiveDestinations = new ArrayList<>();
    private BukkitTask locationRefresher;
    private BukkitTask compassTask;
    private BossBar compassBar;
    private boolean questIsDone = false;
    private boolean stopped = false;

    public QuestTracking(Player player, CustomQuest customQuest) {
        this.player = player;
        this.customQuest = customQuest;
        startLocationGetter();
        startCompass();
        playerTrackingQuests.put(player.getUniqueId(), this);
        customQuest.getQuestObjectives().displayLazyObjectivesScoreboard(player);
    }

    public static boolean isTracking(Player player) {
        return playerTrackingQuests.containsKey(player.getUniqueId());
    }

    public static void shutdown() {
        new HashMap<>(playerTrackingQuests).values().forEach(QuestTracking::stop);
    }

    public static void toggleTracking(Player player, String questID) {
        CustomQuest customQuest = (CustomQuest) PlayerData.getQuest(player.getUniqueId(), questID);
        if (customQuest == null) {
            player.sendMessage(QuestsConfig.getQuestTrackingInvalidMessage());
            return;
        }
        toggleTracking(player, customQuest);
    }

    public static void toggleTracking(Player player, CustomQuest quest) {
        if (playerTrackingQuests.containsKey(player.getUniqueId())) {
            playerTrackingQuests.get(player.getUniqueId()).stop();
        } else {
            if (quest == null) {
                player.sendMessage(QuestsConfig.getQuestTrackingInvalidMessage());
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
        }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L * 60L);
    }

    public void updateLocations(Quest quest) {
        List<ObjectiveDestinations> destinations = new ArrayList<>();
        if (!quest.getQuestObjectives().isOver()) {
            questIsDone = false;
            turnInNPCs.clear();
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (objective != null && !objective.isObjectiveCompleted())
                    if (objective instanceof CustomKillObjective)
                        destinations.addAll(getKillLocations((CustomKillObjective) objective));
                    else if (objective instanceof DialogObjective)
                        destinations.addAll(getDialogLocations((DialogObjective) objective));
                    else if (objective instanceof CustomFetchObjective)
                        destinations.addAll(getFetchLocations((CustomFetchObjective) objective));
            objectiveDestinations = destinations;
        } else {
            questIsDone = true;
            getTurnInNPC();
        }
    }

    private List<ObjectiveDestinations> getKillLocations(CustomKillObjective customKillObjective) {
        List<ObjectiveDestinations> destinations = new ArrayList<>();
        List<Location> locations = getCustomBossLocations(customKillObjective.getCustomBossFilename());
        destinations.add(new ObjectiveDestinations(customKillObjective, locations));
        return destinations;
    }

    private List<ObjectiveDestinations> getDialogLocations(DialogObjective dialogObjective) {
        List<ObjectiveDestinations> destinations = new ArrayList<>();
        destinations.add(new ObjectiveDestinations(dialogObjective, getNPCLocations(dialogObjective.getNpcFilename())));
        return destinations;
    }

    private List<ObjectiveDestinations> getFetchLocations(CustomFetchObjective customFetchObjective) {
        List<ObjectiveDestinations> destinations = new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        CustomBossesConfig.getCustomBosses().values().forEach(customBossesConfigFields -> {
            if (customBossesConfigFields.getCustomLootTable() == null) return;
            if (dropsCustomItem(customBossesConfigFields.getCustomLootTable().getEntries(), customFetchObjective.getKey()))
                getCustomBossLocations(customBossesConfigFields.getFilename()).forEach(location -> addLocation(locations, location));
        });
        new ArrayList<>(TreasureChest.getTreasureChestHashMap().values()).forEach((treasureChest -> {
            if (treasureChest.getCustomTreasureChestConfigFields().getCustomLootTable() == null) return;
            if (dropsCustomItem(treasureChest.getCustomTreasureChestConfigFields().getCustomLootTable().getEntries(), customFetchObjective.getKey()))
                addLocation(locations, treasureChest.getLocation());
        }));
        destinations.add(new ObjectiveDestinations(customFetchObjective, locations));
        return destinations;
    }

    private void getTurnInNPC() {
        turnInNPCs.clear();
        turnInNPCs.addAll(getNPCLocations(customQuest.getQuestTaker()));
    }

    private boolean dropsCustomItem(List<CustomLootEntry> customLootEntries, String itemFilename) {
        if (customLootEntries == null || itemFilename == null) return false;
        for (CustomLootEntry customLootEntry : customLootEntries)
            if (customLootEntry instanceof EliteCustomLootEntry eliteCustomLootEntry && eliteCustomLootEntry.getFilename().equals(itemFilename))
                return true;
        return false;
    }

    private List<Location> getNPCLocations(String npcFilename) {
        List<Location> locations = new ArrayList<>();
        if (npcFilename == null) return locations;
        NPCsConfigFields npcsConfigFields = NPCsConfig.getNpcEntities().get(npcFilename);
        if (npcsConfigFields == null) return locations;
        addLocationStrings(locations, npcsConfigFields.getLocations());
        addLocationString(locations, npcsConfigFields.getSpawnLocation());
        return locations;
    }

    private List<Location> getCustomBossLocations(String customBossFilename) {
        List<Location> locations = new ArrayList<>();
        if (customBossFilename == null) return locations;
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(customBossFilename);
        if (customBossesConfigFields != null) {
            RegionalBossEntity.getRegionalBossEntities(customBossesConfigFields)
                    .forEach(regionalBossEntity -> addRegionalBossLocation(locations, regionalBossEntity));
            if (locations.isEmpty())
                addLocationStrings(locations, customBossesConfigFields.getSpawnLocations());
        }
        if (locations.isEmpty())
            addLoadedCustomBossLocations(locations, customBossFilename);
        return locations;
    }

    private void addRegionalBossLocation(List<Location> locations, RegionalBossEntity regionalBossEntity) {
        Location location = regionalBossEntity.getLocation();
        if (location == null)
            location = regionalBossEntity.getPersistentLocation();
        addLocation(locations, location);
    }

    private void addLoadedCustomBossLocations(List<Location> locations, String customBossFilename) {
        new ArrayList<EliteEntity>(EntityTracker.getEliteMobEntities().values()).forEach(eliteEntity -> {
            if (!(eliteEntity instanceof CustomBossEntity customBossEntity)) return;
            if (customBossMatches(customBossEntity, customBossFilename))
                addLocation(locations, eliteEntity.getLocation());
        });
    }

    private boolean customBossMatches(CustomBossEntity customBossEntity, String customBossFilename) {
        if (customBossEntity.getPhaseBossEntity() != null)
            return customBossEntity.getPhaseBossEntity().getPhase1Config().getFilename().equals(customBossFilename);
        return customBossEntity.getCustomBossesConfigFields().getFilename().equals(customBossFilename);
    }

    private void addLocationStrings(List<Location> locations, List<String> rawLocations) {
        if (rawLocations == null) return;
        rawLocations.forEach(rawLocation -> addLocationString(locations, rawLocation));
    }

    private void addLocationString(List<Location> locations, String rawLocation) {
        if (rawLocation == null || rawLocation.isBlank()) return;
        addLocation(locations, ConfigurationLocation.serialize(rawLocation, true));
    }

    private void addLocation(List<Location> locations, Location location) {
        if (location == null || location.getWorld() == null) return;
        for (Location existingLocation : locations)
            if (isSameBlockLocation(existingLocation, location))
                return;
        locations.add(location);
    }

    private boolean isSameBlockLocation(Location location1, Location location2) {
        if (location1 == null || location2 == null || location1.getWorld() == null || location2.getWorld() == null)
            return false;
        return location1.getWorld().equals(location2.getWorld()) &&
                location1.getBlockX() == location2.getBlockX() &&
                location1.getBlockY() == location2.getBlockY() &&
                location1.getBlockZ() == location2.getBlockZ();
    }

    public void stop() {
        if (stopped) return;
        stopped = true;
        playerTrackingQuests.remove(player.getUniqueId());
        resetPlayerScoreboard();
        if (locationRefresher != null) locationRefresher.cancel();
        if (compassTask != null) compassTask.cancel();
        if (compassBar != null) compassBar.removeAll();
    }

    private void resetPlayerScoreboard() {
        if (!player.isOnline()) return;
        Runnable resetScoreboard = () -> {
            if (!player.isOnline() || Bukkit.getScoreboardManager() == null) return;
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        };
        if (Bukkit.isPrimaryThread()) {
            resetScoreboard.run();
        } else if (MetadataHandler.PLUGIN != null && MetadataHandler.PLUGIN.isEnabled()) {
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, resetScoreboard);
        }
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
        // While quest dialogue is showing, hide the compass bar and scoreboard so they don't clutter
        // the dialogue box. This must be gated here because the compass re-adds the player every tick.
        if (QuestsConfig.isHideQuestScoreboardDuringQuestDialogue()
                && QuestDialogueBossBarManager.hasActiveSession(player)) {
            compassBar.removePlayer(player);
            return;
        }
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
                    Location wormholeLocation = WormholeNavigation.findDirectWormholeEntry(player.getWorld(), world);
                    if (wormholeLocation != null) {
                        LocationAndSymbol pair = processLocations(wormholeLocation, null);
                        if (pair != null)
                            compassText = compassText.substring(0, pair.getKey()) + pair.getValue() + compassText.substring(pair.getKey() + 1);
                        wormholeIsViable = true;
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
        if (location == null || location.getWorld() == null) return null;
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
            QuestTracking questTracking = getPlayerTrackingQuests().get(event.getPlayer().getUniqueId());
            if (questTracking == null) return;
            questTracking.stop();
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestProgressEvent(QuestProgressionEvent event) {
            //if (event.getObjective().isObjectiveCompleted()) return;
            if (!isTracking(event.getPlayer())) return;
            if (!getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).getCustomQuest().getQuestID().equals(event.getQuest().getQuestID()))
                return;
            getPlayerTrackingQuests().get(event.getPlayer().getUniqueId())
                    .updateLocations(getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).getCustomQuest());
            getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).refreshScoreboard();
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestCompleteEvent(QuestCompleteEvent event) {
            if (!isTracking(event.getPlayer())) return;
            if (!getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).getCustomQuest().getQuestID().equals(event.getQuest().getQuestID()))
                return;
            getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).stop();
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onQuestAcceptEvent(QuestAcceptEvent event) {
            if (!(event.getQuest() instanceof CustomQuest)) return;
            if (!((CustomQuest) event.getQuest()).getCustomQuestsConfigFields().isTrackable()) return;
            if (QuestsConfig.isAutoTrackQuestsOnAccept()) {
                toggleTracking(event.getPlayer(), (CustomQuest) event.getQuest());
                event.getPlayer().spigot().sendMessage(SpigotMessage.commandHoverMessage(
                        QuestsConfig.getChatTrackingMessage(),
                        QuestsConfig.getChatTrackingHover(),
                        QuestsConfig.getChatTrackingCommand().replace("$questID", event.getQuest().getQuestID().toString())
                ));
            } else
                event.getPlayer().spigot().sendMessage(SpigotMessage.commandHoverMessage(
                        QuestsConfig.getChatTrackMessage(),
                        QuestsConfig.getChatTrackHover(),
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
