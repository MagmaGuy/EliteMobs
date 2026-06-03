package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.NPCProximityEnterEvent;
import com.magmaguy.elitemobs.api.NPCProximityLeaveEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.scripts.NPCScriptHook;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NPCProximitySensor implements Listener {

    private static final NPCProximityState proximityState = new NPCProximityState();
    private static BukkitTask proximityScanTask = null;

    public NPCProximitySensor() {
        proximityScanTask = new BukkitRunnable() {

            @Override
            public void run() {
                Collection<NPCEntity> npcEntities = EntityTracker.getNpcEntities().values();
                if (npcEntities.isEmpty() || Bukkit.getOnlinePlayers().isEmpty()) {
                    proximityState.clear();
                    return;
                }

                Map<NPCProximityKey, ProximityDetection> detections = new HashMap<>(Math.max(16, npcEntities.size()));
                for (NPCEntity npcEntity : npcEntities) {
                    LivingEntity villager = npcEntity.getVillager();
                    if (villager == null || !villager.isValid()) continue;
                    double activationRadius = npcEntity.getNPCsConfigFields().getActivationRadius();
                    if (activationRadius <= 0) continue;
                    double activationRadiusSquared = activationRadius * activationRadius;
                    Location npcLocation = villager.getLocation();
                    for (Entity entity : villager.getNearbyEntities(activationRadius, activationRadius, activationRadius)) {
                        if (!(entity instanceof Player player)) continue;
                        if (!player.isValid()) continue;
                        Location playerLocation = player.getLocation();
                        if (playerLocation.getWorld() == null || npcLocation.getWorld() == null ||
                                !playerLocation.getWorld().getUID().equals(npcLocation.getWorld().getUID()) ||
                                playerLocation.distanceSquared(npcLocation) > activationRadiusSquared)
                            continue;
                        Vector direction = playerLocation.toVector().subtract(npcLocation.toVector());
                        if (direction.lengthSquared() > 0) {
                            villager.teleport(npcLocation.clone().setDirection(direction));
                        }
                        detections.put(new NPCProximityKey(npcEntity.getUuid(), player.getUniqueId()), new ProximityDetection(npcEntity, player));
                    }
                }

                NPCProximityState.ProximityChanges changes = proximityState.update(detections.keySet());
                for (Map.Entry<NPCProximityKey, ProximityDetection> entry : detections.entrySet()) {
                    ProximityDetection detection = entry.getValue();
                    if (changes.entered().contains(entry.getKey())) {
                        handleEnter(detection.npcEntity(), detection.player());
                    } else if (!detection.npcEntity().getNPCsConfigFields().getInteractionType().equals(NPCInteractions.NPCInteractionType.CHAT)) {
                        detection.npcEntity().sayDialog(detection.player());
                    }
                }
                for (NPCProximityKey leftKey : changes.left()) {
                    NPCEntity npcEntity = EntityTracker.getNpcEntities().get(leftKey.npcUuid());
                    Player player = Bukkit.getPlayer(leftKey.playerUuid());
                    if (npcEntity != null && player != null) {
                        handleLeave(npcEntity, player);
                    }
                }
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20L * 5L);

    }

    public static void shutdown() {
        if (proximityScanTask != null && !proximityScanTask.isCancelled()) {
            proximityScanTask.cancel();
            proximityScanTask = null;
        }
        proximityState.clear();
    }

    private void handleEnter(NPCEntity npcEntity, Player player) {
        NPCProximityEnterEvent event = new NPCProximityEnterEvent(npcEntity, player, npcEntity.getNPCsConfigFields().getActivationRadius());
        new EventCaller(event);
        npcEntity.runScripts(NPCScriptHook.ON_PROXIMITY_ENTER, event, player);
        npcEntity.sayGreeting(player);
        startQuestIndicator(npcEntity, player);
    }

    private void handleLeave(NPCEntity npcEntity, Player player) {
        NPCProximityLeaveEvent event = new NPCProximityLeaveEvent(npcEntity, player, npcEntity.getNPCsConfigFields().getActivationRadius());
        new EventCaller(event);
        npcEntity.runScripts(NPCScriptHook.ON_PROXIMITY_LEAVE, event, player);
    }

    private record ProximityDetection(NPCEntity npcEntity, Player player) {
    }

    private void startQuestIndicator(NPCEntity npcEntity, Player player) {
        if (!npcEntity.getNPCsConfigFields().getInteractionType().equals(NPCInteractions.NPCInteractionType.QUEST_GIVER) &&
                !npcEntity.getNPCsConfigFields().getInteractionType().equals(NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER))
            return;
        findQuestState(npcEntity, player);

    }

    private void findQuestState(NPCEntity npcEntity, Player player) {
        //Case for NPCs needed for quests but who are not themselves quest givers
        if (npcEntity.getNPCsConfigFields().getQuestFilenames() == null) return;
        if (npcEntity.getNPCsConfigFields().getInteractionType().equals(NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER)) {
            if (!PlayerData.getQuests(player.getUniqueId()).isEmpty()) {
                for (String questString : npcEntity.getNPCsConfigFields().getQuestFilenames())
                    for (Quest quest : PlayerData.getQuests(player.getUniqueId()))
                        if (quest instanceof CustomQuest &&
                                questString.equals(((CustomQuest) quest).getCustomQuestsConfigFields().getFilename()))
                            if (!quest.isAccepted()) {
                                if (!((CustomQuest) quest).hasPermissionForQuest(player))
                                    return;
                                //Quest yet to be accepted
                                unacceptedQuestIndicator(npcEntity, player);
                                return;
                            } else if (!quest.getQuestObjectives().isOver()) {
                                //Quest has been accepted but not completed
                                acceptedQuestIndicator(npcEntity, player);
                                return;
                            } else {
                                //Quest has been completed
                                completedQuestIndicator(npcEntity, player);
                                return;
                            }
            }
        } else if (npcEntity.getNPCsConfigFields().getInteractionType().equals(NPCInteractions.NPCInteractionType.QUEST_GIVER)) {
            for (Quest quest : PlayerData.getQuests(player.getUniqueId()))
                if (quest instanceof DynamicQuest)
                    //Dynamic quest
                    if (!quest.isAccepted()) {
                        //Quest yet to be accepted
                        unacceptedQuestIndicator(npcEntity, player);
                        return;
                    } else if (!quest.getQuestObjectives().isOver()) {
                        //Quest has been accepted but not completed
                        acceptedQuestIndicator(npcEntity, player);
                        return;
                    } else {
                        //Quest has been completed
                        completedQuestIndicator(npcEntity, player);
                        return;
                    }
        }
        unacceptedQuestIndicator(npcEntity, player);
    }

    private void unacceptedQuestIndicator(NPCEntity npcEntity, Player player) {
        generateIndicator(npcEntity, player, ChatColor.YELLOW + "" + ChatColor.BOLD + "!",
                ChatColor.GOLD + "" + ChatColor.BOLD + "!");
    }

    private void acceptedQuestIndicator(NPCEntity npcEntity, Player player) {
        generateIndicator(npcEntity, player, ChatColor.GRAY + "" + ChatColor.BOLD + "?",
                ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "?");
    }

    private void completedQuestIndicator(NPCEntity npcEntity, Player player) {
        generateIndicator(npcEntity, player, ChatColor.YELLOW + "" + ChatColor.BOLD + "?",
                ChatColor.GOLD + "" + ChatColor.BOLD + "?");
    }

    private void generateIndicator(NPCEntity npcEntity, Player player, String messageUp, String messageDown) {
        Location newLocation = npcEntity.getVillager().getEyeLocation().clone()
                .add(player.getLocation().clone().subtract(npcEntity.getVillager().getLocation()).toVector().normalize().multiply(0.5))
                .add(new Vector(0, -0.1, 0));
        FakeText fakeText = VisualDisplay.generateFakeText(newLocation, messageUp, player);
        if (fakeText == null) return;

        AtomicInteger counter = new AtomicInteger();
        AtomicBoolean up = new AtomicBoolean(true);
        final Location[] currentLocation = {newLocation.clone()};

        Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, task -> {
            if (!player.isValid() ||
                    npcEntity.getVillager() == null ||
                    !npcEntity.getVillager().isValid() ||
                    !npcEntity.getVillager().getWorld().equals(player.getWorld()) ||
                    npcEntity.getVillager().getLocation().distance(player.getLocation()) > npcEntity.getNPCsConfigFields().getActivationRadius()) {
                task.cancel();
                fakeText.remove();
                return;
            }

            counter.getAndIncrement();

            if (counter.get() % 20 == 0) {
                up.getAndSet(!up.get());
                if (up.get())
                    fakeText.setText(messageUp);
                else
                    fakeText.setText(messageDown);
            }

            currentLocation[0].add(new Vector(0, up.get() ? 0.01 : -0.01, 0));
            fakeText.teleport(currentLocation[0]);

        }, 0L, 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().length() > 0)
            for (Entity entity : event.getPlayer().getNearbyEntities(5, 5, 5))
                if (EntityTracker.isNPCEntity(entity))
                    EntityTracker.getNPCEntity(entity).sayFarewell((Player) event.getPlayer());
    }

}
