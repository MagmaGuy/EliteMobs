package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NPCProximitySensor implements Listener {

    private static final HashSet<Player> nearbyPlayers = new HashSet<>();

    public NPCProximitySensor() {
        new BukkitRunnable() {

            @Override
            public void run() {
                HashSet<Player> unseenPlayerList = (HashSet<Player>) nearbyPlayers.clone();
                for (NPCEntity npcEntity : EntityTracker.getNpcEntities().values()) {
                    if (!npcEntity.isValid()) continue;
                    for (Entity entity : npcEntity.getVillager().getNearbyEntities(npcEntity.getNPCsConfigFields().getActivationRadius(),
                            npcEntity.getNPCsConfigFields().getActivationRadius(), npcEntity.getNPCsConfigFields().getActivationRadius())) {
                        if (!entity.getType().equals(EntityType.PLAYER)) continue;
                        Player player = (Player) entity;
                        Location rotatedLocation = npcEntity.getVillager().getLocation().setDirection(entity.getLocation().subtract(npcEntity.getVillager().getLocation()).toVector());
                        npcEntity.getVillager().teleport(rotatedLocation);
                        if (unseenPlayerList.contains(player)) {
                            if (!npcEntity.getNPCsConfigFields().getInteractionType().equals(NPCInteractions.NPCInteractionType.CHAT))
                                npcEntity.sayDialog(player);
                            unseenPlayerList.remove(player);
                        } else {
                            npcEntity.sayGreeting(player);
                            nearbyPlayers.add(player);
                            startQuestIndicator(npcEntity, player);
                        }
                    }
                }

                nearbyPlayers.removeIf(unseenPlayerList::contains);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20L * 5L);

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
        TextDisplay visualArmorStand = VisualDisplay.generateTemporaryTextDisplay(newLocation, messageUp);
        AtomicInteger counter = new AtomicInteger();
        AtomicBoolean up = new AtomicBoolean(true);
        Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, task -> {
            if (!player.isValid() ||
                    npcEntity.getVillager() == null ||
                    !npcEntity.getVillager().isValid() ||
                    !npcEntity.getVillager().getWorld().equals(player.getWorld()) ||
                    npcEntity.getVillager().getLocation().distance(player.getLocation()) > npcEntity.getNPCsConfigFields().getActivationRadius()) {
                task.cancel();
                EntityTracker.unregister(visualArmorStand, RemovalReason.EFFECT_TIMEOUT);
                return;
            }

            counter.getAndIncrement();

            if (counter.get() % 20 == 0) {
                up.getAndSet(!up.get());
                if (up.get())
                    visualArmorStand.setCustomName(messageUp);
                else
                    visualArmorStand.setCustomName(messageDown);
            }

            visualArmorStand.teleport(visualArmorStand.getLocation().clone().add(new Vector(0, up.get() ? 0.01 : -0.01, 0)));

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
