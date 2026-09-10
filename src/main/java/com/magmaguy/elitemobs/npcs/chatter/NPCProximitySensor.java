package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.NPCEntityRemoveEvent;
import com.magmaguy.elitemobs.api.NPCProximityEnterEvent;
import com.magmaguy.elitemobs.api.NPCProximityLeaveEvent;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.scripts.ScriptableNPC;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolEditor;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolService;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class NPCProximitySensor implements Listener {

    private static final int BOUNCE_PERIOD_TICKS = 40;
    private static final double BOUNCE_HEIGHT = 0.16;
    private static final NPCProximityState proximityState = new NPCProximityState();
    private static BukkitTask proximityScanTask = null;
    private static BukkitTask questIndicatorTask;
    private static int indicatorAnimationTick;
    private static double indicatorBounceOffset;
    private static final Map<NPCProximityKey, QuestIndicator> questIndicators = new HashMap<>();

    public NPCProximitySensor() {
        proximityScanTask = new BukkitRunnable() {

            @Override
            public void run() {
                Collection<NPCEntity> npcEntities = EntityTracker.getNpcEntities().values();
                if (npcEntities.isEmpty() || Bukkit.getOnlinePlayers().isEmpty()) {
                    proximityState.clear();
                    removeIndicators(key -> true);
                    return;
                }

                Map<NPCProximityKey, ProximityDetection> detections = new HashMap<>(Math.max(16, npcEntities.size()));
                for (NPCEntity npcEntity : npcEntities) {
                    LivingEntity villager = npcEntity.getVillager();
                    if (villager == null || !villager.isValid()) continue;
                    if (PatrolService.isActivelyMoving(npcEntity) || PatrolEditor.isEditing(npcEntity)) continue;
                    double activationRadius = npcEntity.getNPCsConfigFields().getActivationRadius();
                    if (activationRadius <= 0) continue;
                    double activationRadiusSquared = activationRadius * activationRadius;
                    Location npcLocation = villager.getLocation();
                    boolean patrolOwnsFacing = npcEntity.getNPCsConfigFields().isPatrolFaceNearbyPlayers()
                            && PatrolService.hasConfiguredPatrol(npcEntity);
                    for (Entity entity : villager.getNearbyEntities(activationRadius, activationRadius, activationRadius)) {
                        if (!(entity instanceof Player player)) continue;
                        if (!player.isValid()) continue;
                        Location playerLocation = player.getLocation();
                        if (playerLocation.getWorld() == null || npcLocation.getWorld() == null ||
                                !playerLocation.getWorld().getUID().equals(npcLocation.getWorld().getUID()) ||
                                playerLocation.distanceSquared(npcLocation) > activationRadiusSquared)
                            continue;
                        Vector direction = playerLocation.toVector().subtract(npcLocation.toVector());
                        if (!patrolOwnsFacing && direction.lengthSquared() > 0) {
                            villager.teleport(npcLocation.clone().setDirection(direction));
                        }
                        detections.put(new NPCProximityKey(npcEntity.getUuid(), player.getUniqueId()), new ProximityDetection(npcEntity, player));
                    }
                }

                NPCProximityState.ProximityChanges changes = proximityState.update(detections.keySet());
                for (Map.Entry<NPCProximityKey, ProximityDetection> entry : detections.entrySet()) {
                    ProximityDetection detection = entry.getValue();
                    updateQuestIndicator(detection.npcEntity(), detection.player());
                    if (changes.entered().contains(entry.getKey())) {
                        handleEnter(detection.npcEntity(), detection.player());
                    } else if (!detection.npcEntity().getNPCsConfigFields().getInteractionType().equals(NPCInteractions.NPCInteractionType.CHAT)) {
                        detection.npcEntity().sayDialog(detection.player());
                    }
                }
                for (NPCProximityKey leftKey : changes.left()) {
                    QuestIndicator indicator = questIndicators.remove(leftKey);
                    if (indicator != null) indicator.remove();
                    NPCEntity npcEntity = EntityTracker.getNpcEntities().get(leftKey.npcUuid());
                    Player player = Bukkit.getPlayer(leftKey.playerUuid());
                    if (npcEntity != null && player != null) {
                        handleLeave(npcEntity, player);
                    }
                }
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20L * 5L);
        questIndicatorTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN,
                NPCProximitySensor::refreshQuestIndicators, 1L, 1L);
    }

    public static void shutdown() {
        if (proximityScanTask != null && !proximityScanTask.isCancelled()) {
            proximityScanTask.cancel();
            proximityScanTask = null;
        }
        proximityState.clear();
        if (questIndicatorTask != null) questIndicatorTask.cancel();
        questIndicatorTask = null;
        indicatorAnimationTick = 0;
        indicatorBounceOffset = 0;
        removeIndicators(key -> true);
    }

    private void handleEnter(NPCEntity npcEntity, Player player) {
        NPCProximityEnterEvent event = new NPCProximityEnterEvent(npcEntity, player, npcEntity.getNPCsConfigFields().getActivationRadius());
        new EventCaller(event);
        npcEntity.runScripts(ScriptableNPC.ON_PROXIMITY_ENTER, event, player);
        npcEntity.sayGreeting(player);
    }

    private void handleLeave(NPCEntity npcEntity, Player player) {
        NPCProximityLeaveEvent event = new NPCProximityLeaveEvent(npcEntity, player, npcEntity.getNPCsConfigFields().getActivationRadius());
        new EventCaller(event);
        npcEntity.runScripts(ScriptableNPC.ON_PROXIMITY_LEAVE, event, player);
    }

    private record ProximityDetection(NPCEntity npcEntity, Player player) {
    }

    private static void updateQuestIndicator(NPCEntity npcEntity, Player player) {
        var type = npcEntity.getNPCsConfigFields().getInteractionType();
        if (type != NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER
                && type != NPCInteractions.NPCInteractionType.QUEST_GIVER) return;
        NPCProximityKey key = new NPCProximityKey(npcEntity.getUuid(), player.getUniqueId());
        questIndicators.computeIfAbsent(key, ignored -> new QuestIndicator()).update(npcEntity, player, true);
    }

    private static void refreshQuestIndicators() {
        indicatorAnimationTick = (indicatorAnimationTick + 1) % BOUNCE_PERIOD_TICKS;
        // Bounce upward from the configured marker anchor.
        indicatorBounceOffset = BOUNCE_HEIGHT * 0.5 * (1 + Math.sin(
                2 * Math.PI * indicatorAnimationTick / BOUNCE_PERIOD_TICKS - Math.PI / 2));
        boolean refreshQuestState = indicatorAnimationTick % 10 == 0;
        var iterator = questIndicators.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            NPCEntity npc = EntityTracker.getNpcEntities().get(entry.getKey().npcUuid());
            Player player = Bukkit.getPlayer(entry.getKey().playerUuid());
            if (npc == null || player == null || !player.isValid()
                    || npc.getVillager() == null || !npc.getVillager().isValid()
                    || PatrolService.isActivelyMoving(npc) || PatrolEditor.isEditing(npc)
                    || !npc.getVillager().getWorld().equals(player.getWorld())
                    || npc.getNPCsConfigFields().getActivationRadius() <= 0
                    || npc.getVillager().getLocation().distanceSquared(player.getLocation())
                    > Math.pow(npc.getNPCsConfigFields().getActivationRadius(), 2)) {
                entry.getValue().remove();
                iterator.remove();
            } else {
                entry.getValue().update(npc, player, refreshQuestState);
            }
        }
    }

    private static String findQuestState(NPCEntity npcEntity, Player player) {
        if (!PlayerData.isInMemory(player)) return null;
        List<Quest> quests = PlayerData.getQuests(player.getUniqueId());
        if (quests == null) return null;
        var type = npcEntity.getNPCsConfigFields().getInteractionType();
        boolean dynamic = type == NPCInteractions.NPCInteractionType.QUEST_GIVER;
        if (!dynamic && type != NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER) return null;
        if (dynamic && !player.hasPermission("elitemobs.quest.npc")) return null;

        Set<String> activeCustomQuests = new HashSet<>();
        for (Quest quest : quests) {
            if (quest.getQuestObjectives().isTurnedIn()) continue;
            if (quest instanceof CustomQuest customQuest && quest.isAccepted())
                activeCustomQuests.add(customQuest.getConfigurationFilename());
            boolean canTurnInHere = dynamic ? quest instanceof DynamicQuest
                    : quest instanceof CustomQuest
                    && npcEntity.getNPCsConfigFields().getFilename().equals(quest.getQuestTaker());
            // Inspect every turn-in before considering offers, including NPCs with no offer list.
            if (canTurnInHere && quest.isAccepted() && quest.getQuestObjectives().isOver())
                return ChatColor.YELLOW + "" + ChatColor.BOLD + "?";
        }

        if (dynamic)
            return DynamicQuest.hasAvailableQuests(player) ? ChatColor.YELLOW + "" + ChatColor.BOLD + "!" : null;
        if (npcEntity.getNPCsConfigFields().getQuestFilenames() != null)
            for (String filename : npcEntity.getNPCsConfigFields().getQuestFilenames()) {
                if (activeCustomQuests.contains(filename)) continue;
                if (CustomQuest.hasPermissionForQuest(player, CustomQuestsConfig.getCustomQuests().get(filename)))
                    return ChatColor.YELLOW + "" + ChatColor.BOLD + "!";
            }
        return null;
    }

    private static void removeIndicators(Predicate<NPCProximityKey> matches) {
        var iterator = questIndicators.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (!matches.test(entry.getKey())) continue;
            entry.getValue().remove();
            iterator.remove();
        }
    }

    /** One packet display per NPC/player pair; state changes update that display in place. */
    private static final class QuestIndicator {
        private FakeText display;

        private void update(NPCEntity npc, Player player, boolean refreshQuestState) {
            // Animate every tick, retaining the existing half-second quest-state refresh cadence.
            String text = refreshQuestState ? findQuestState(npc, player)
                    : display == null ? null : display.getText();
            if (text == null) {
                remove();
                return;
            }
            Location location = npc.getQuestIndicatorLocation().add(0, indicatorBounceOffset, 0);
            if (display == null) {
                display = VisualDisplay.createStyledFakeText(location, text, Color.fromARGB(0), true, 3.0f);
                if (display != null) display.displayTo(player);
            } else {
                if (!text.equals(display.getText())) display.setText(text);
                Location previous = display.getLocation();
                if (!location.getWorld().equals(previous.getWorld()) || location.distanceSquared(previous) > 1.0E-8)
                    display.teleport(location);
            }
        }

        private void remove() {
            if (display != null) display.remove();
            display = null;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeIndicators(key -> key.playerUuid().equals(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        removeIndicators(key -> key.playerUuid().equals(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onNPCRemoved(NPCEntityRemoveEvent event) {
        removeIndicators(key -> key.npcUuid().equals(event.getNPCEntity().getUuid()));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().length() > 0)
            for (Entity entity : event.getPlayer().getNearbyEntities(5, 5, 5))
                if (EntityTracker.isNPCEntity(entity))
                    EntityTracker.getNPCEntity(entity).sayFarewell((Player) event.getPlayer());
    }

}
