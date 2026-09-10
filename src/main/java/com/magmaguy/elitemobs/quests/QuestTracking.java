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
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.items.customloottable.CustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.parties.PartySidebar;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.*;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
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
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class QuestTracking {

    private static final double VERTICAL_ENTER_RADIUS = 4;
    private static final double VERTICAL_EXIT_RADIUS = 6;
    private static final double VERTICAL_ENTER_HEIGHT = 5;
    private static final double VERTICAL_EXIT_HEIGHT = 3;

    @Getter
    private static final HashMap<UUID, QuestTracking> playerTrackingQuests = new HashMap<>();
    private final Player player;
    @Getter
    private final Quest quest;
    private final List<Location> turnInNPCs = new ArrayList<>();
    private List<ObjectiveDestinations> objectiveDestinations = new ArrayList<>();
    private BukkitTask locationRefresher;
    private BukkitTask compassTask;
    private BossBar compassBar;
    private boolean questIsDone = false;
    private boolean stopped = false;
    private boolean wasWaiting;
    private boolean refreshQueued;
    private Set<VerticalDestination> previousVerticalDestinations = new HashSet<>();
    private Set<VerticalDestination> verticalDestinations = new HashSet<>();
    private boolean targetsAbove;
    private boolean targetsBelow;

    private void queueLocationRefresh() {
        if (stopped || refreshQueued) return;
        refreshQueued = true;
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            refreshQueued = false;
            if (!stopped && player.isOnline()) updateLocations(quest);
        });
    }

    public QuestTracking(Player player, Quest quest) {
        this.player = player;
        this.quest = quest;
        startLocationGetter();
        startCompass();
        playerTrackingQuests.put(player.getUniqueId(), this);
        quest.getQuestObjectives().displayLazyObjectivesScoreboard(player);
    }

    public static boolean isTracking(Player player) {
        return playerTrackingQuests.containsKey(player.getUniqueId());
    }

    public static void shutdown() {
        new HashMap<>(playerTrackingQuests).values().forEach(QuestTracking::stop);
    }

    public static void toggleTracking(Player player, String questID) {
        Quest quest = PlayerData.getQuest(player.getUniqueId(), questID);
        if (quest == null) {
            player.sendMessage(QuestsConfig.getQuestTrackingInvalidMessage());
            return;
        }
        toggleTracking(player, quest);
    }

    public static void toggleTracking(Player player, Quest quest) {
        if (playerTrackingQuests.containsKey(player.getUniqueId())) {
            playerTrackingQuests.get(player.getUniqueId()).stop();
        } else {
            if (quest == null) {
                player.sendMessage(QuestsConfig.getQuestTrackingInvalidMessage());
                return;
            }
            //Only custom quests carry a trackable flag; dynamic quests are always trackable
            if (quest instanceof CustomQuest customQuest && !customQuest.getCustomQuestsConfigFields().isTrackable())
                return;
            new QuestTracking(player, quest);
        }
    }

    public void refreshScoreboard() {
        quest.getQuestObjectives().displayLazyObjectivesScoreboard(player);
    }

    private void startLocationGetter() {
        locationRefresher = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isValid()) {
                    stop();
                    return;
                }
                updateLocations(quest);
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
                    else if (objective instanceof ClassUnlockObjective unlock && unlock.getNpcFilename() != null)
                        destinations.add(new ObjectiveDestinations(unlock, getNPCLocations(unlock.getNpcFilename())));
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
        turnInNPCs.addAll(getNPCLocations(quest.getQuestTaker()));
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
        if (npcsConfigFields == null || !npcsConfigFields.isEnabled()) return locations;
        // Runtime NPCs carry the cloned world and their current patrol position.
        for (var npc : EntityTracker.getNpcEntities().values()) {
            if (!npcFilename.equals(npc.getNPCsConfigFields().getFilename())) continue;
            Location location = npc.getPersistentLocation();
            if (location != null && player.getWorld().equals(location.getWorld()))
                addLocation(locations, location);
        }
        if (!locations.isEmpty()) return locations;
        if (npcsConfigFields.isInstanced()) {
            // NPCs may not have materialized yet. Resolve their authored coordinates only
            // against this player's matching dungeon, never another party's copy or blueprint.
            if (PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeon
                    && player.getWorld().equals(dungeon.getWorld())) {
                List<String> authored = new ArrayList<>();
                if (npcsConfigFields.getLocations() != null) authored.addAll(npcsConfigFields.getLocations());
                if (npcsConfigFields.getSpawnLocation() != null) authored.add(npcsConfigFields.getSpawnLocation());
                for (String raw : authored)
                    if (raw != null && !raw.isBlank() && dungeon.getContentPackagesConfigFields().getWorldName()
                            .equals(ConfigurationLocation.worldName(raw)))
                        addLocation(locations, ConfigurationLocation.serializeWithInstance(dungeon.getWorld(), raw));
            }
            return locations;
        }
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
        if (PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeon
                && dungeon.getContentPackagesConfigFields().getWorldName().equals(ConfigurationLocation.worldName(rawLocation))) {
            addLocation(locations, ConfigurationLocation.serializeWithInstance(dungeon.getWorld(), rawLocation));
            return;
        }
        addLocation(locations, ConfigurationLocation.serialize(rawLocation, true));
    }

    private void addLocation(List<Location> locations, Location location) {
        if (location == null || location.getWorld() == null) return;
        // A loaded copy belonging to another party must not suppress the authored
        // destination fallback for the player's own instance.
        if (PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeon
                && !location.getWorld().equals(dungeon.getWorld())) return;
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
        previousVerticalDestinations.clear();
        verticalDestinations.clear();
        playerTrackingQuests.remove(player.getUniqueId());
        resetPlayerScoreboard();
        if (locationRefresher != null) locationRefresher.cancel();
        if (compassTask != null) compassTask.cancel();
        if (compassBar != null) {
            BossBarOrderManager.hide(player, compassBar);
            compassBar.removeAll();
        }
    }

    private void resetPlayerScoreboard() {
        if (!player.isOnline()) return;
        Runnable resetScoreboard = () -> {
            if (!player.isOnline() || Bukkit.getScoreboardManager() == null) return;
            if (PartySidebar.isEnabled() && PartyManager.isInParty(player.getUniqueId()))
                PartySidebar.refresh(player);
            else
                SimpleScoreboard.clearScoreboard(player);
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
        // Reuse both sets, retaining hysteresis only for destinations seen in consecutive frames.
        Set<VerticalDestination> reusable = previousVerticalDestinations;
        previousVerticalDestinations = verticalDestinations;
        verticalDestinations = reusable;
        verticalDestinations.clear();
        targetsAbove = false;
        targetsBelow = false;
        var match = PlayerData.getMatchInstance(player);
        boolean waiting = match != null && match.isWaitingPlayer(player);
        if (waiting != wasWaiting) {
            wasWaiting = waiting;
            updateLocations(quest);
        }
        if (waiting) {
            previousVerticalDestinations.clear();
            compassBar.setTitle("Waiting for the dungeon to start");
            BossBarOrderManager.show(player, compassBar);
            return;
        }
        //for reference, character 32 is straight ahead
        String compassText = "---------------------------------------------------------------";
        List<LocationAndSymbol> locationAndSymbols = projectLocations();
        if (!locationAndSymbols.isEmpty() || targetsAbove || targetsBelow)
            for (LocationAndSymbol pair : locationAndSymbols)
                compassText = compassText.substring(0, pair.getKey()) + pair.getValue() + compassText.substring(pair.getKey() + 1);
        else {
            World world = null;
            boolean locationsOutOfBounds = false;
            List<Location> destinations = questIsDone ? new ArrayList<>(turnInNPCs)
                    : objectiveDestinations.stream().flatMap(objective -> objective.getDestinations().stream()).toList();
            for (Location location : destinations)
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

        // Draw last so another objective at the same bearing cannot hide the height cue.
        if (targetsAbove || targetsBelow) {
            String arrow = targetsAbove ? (targetsBelow ? "↕" : "↑") : "↓";
            compassText = compassText.substring(0, 31) + arrow + compassText.substring(32);
        }
        compassBar.setTitle(compassText);
        BossBarOrderManager.show(player, compassBar);
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
            Location playerLocation = player.getLocation();
            double deltaX = location.getX() - playerLocation.getX();
            double deltaY = location.getY() - playerLocation.getY();
            double deltaZ = location.getZ() - playerLocation.getZ();
            double horizontalDistanceSquared = deltaX * deltaX + deltaZ * deltaZ;
            VerticalDestination destination = new VerticalDestination(location.getWorld().getUID(),
                    location.getX(), location.getY(), location.getZ(), deltaY > 0);
            boolean wasVertical = previousVerticalDestinations.contains(destination);
            double radius = wasVertical ? VERTICAL_EXIT_RADIUS : VERTICAL_ENTER_RADIUS;
            // Compare feet to feet so camera pitch, sneaking and eye height do not affect the cue.
            boolean heightRequiresArrow = wasVertical ? Math.abs(deltaY) > VERTICAL_EXIT_HEIGHT
                    : Math.abs(deltaY) >= VERTICAL_ENTER_HEIGHT;
            if (horizontalDistanceSquared <= radius * radius && heightRequiresArrow) {
                verticalDestinations.add(destination);
                if (deltaY > 0) targetsAbove = true;
                else targetsBelow = true;
                return null;
            }
            double angle = getAngle(deltaX, deltaZ, playerLocation.getYaw());
            if (Math.abs(angle) > Math.PI / 2D) return null;
            //Convert to degrees, each character has a resolution of 3 degrees
            return new LocationAndSymbol((int) (angle * 57D / 3D), getSymbol(objective));
        }
        return null;
    }

    private double getAngle(double deltaX, double deltaZ, float yaw) {
        if (deltaX == 0 && deltaZ == 0) return 0;
        double facingX = -Math.sin(Math.toRadians(yaw));
        double facingZ = Math.cos(Math.toRadians(yaw));
        // atan2 also works when looking straight up/down, without normalizing a zero vector.
        return Math.atan2(facingX * deltaZ - facingZ * deltaX, facingX * deltaX + facingZ * deltaZ);
    }

    // Ignore target yaw/pitch when locations refresh; direction changes must cross the entry threshold again.
    private record VerticalDestination(UUID worldId, double x, double y, double z, boolean above) {}

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
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onTargetSpawn(com.magmaguy.elitemobs.api.EliteMobSpawnEvent event) {
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity)) return;
            for (QuestTracking tracking : playerTrackingQuests.values())
                if (tracking.player.getWorld().equals(event.getEntity().getWorld())) tracking.queueLocationRefresh();
        }
        @EventHandler
        public void onWorldChanged(PlayerChangedWorldEvent event) {
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                QuestTracking tracking = getPlayerTrackingQuests().get(event.getPlayer().getUniqueId());
                if (tracking != null) tracking.updateLocations(tracking.getQuest());
            });
        }

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
            if (!getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).getQuest().getQuestID().equals(event.getQuest().getQuestID()))
                return;
            getPlayerTrackingQuests().get(event.getPlayer().getUniqueId())
                    .updateLocations(getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).getQuest());
            getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).refreshScoreboard();
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestCompleteEvent(QuestCompleteEvent event) {
            if (!isTracking(event.getPlayer())) return;
            if (!getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).getQuest().getQuestID().equals(event.getQuest().getQuestID()))
                return;
            getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).stop();
            if (!QuestsConfig.isAutoTrackNextQuestOnCompletion()) return;
            //Completing the tracked quest hands tracking to the player's next active
            //quest instead of leaving the compass empty. Deferred a tick so every
            //completion listener (turn-in, rewards, quest removal) settles first;
            //the completed quest is excluded by ID in case it still lingers.
            Player player = event.getPlayer();
            UUID completedQuestID = event.getQuest().getQuestID();
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                if (!player.isOnline() || isTracking(player)) return;
                List<Quest> activeQuests = PlayerData.getQuests(player.getUniqueId());
                if (activeQuests == null) return;
                for (Quest nextQuest : new ArrayList<>(activeQuests)) {
                    if (nextQuest == null || nextQuest.getQuestID().equals(completedQuestID)) continue;
                    if (nextQuest instanceof CustomQuest customQuest
                            && !customQuest.getCustomQuestsConfigFields().isTrackable()) continue;
                    new QuestTracking(player, nextQuest);
                    player.sendMessage(QuestsConfig.getQuestAutoTrackNextMessage()
                            .replace("$questName", nextQuest.getQuestName() == null ? "" : nextQuest.getQuestName()));
                    return;
                }
            });
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onQuestAcceptEvent(QuestAcceptEvent event) {
            //Dynamic quests are always trackable; only custom quests carry a trackable flag
            if (event.getQuest() instanceof CustomQuest customQuest && !customQuest.getCustomQuestsConfigFields().isTrackable())
                return;
            if (QuestsConfig.isAutoTrackQuestsOnAccept()) {
                toggleTracking(event.getPlayer(), event.getQuest());
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
