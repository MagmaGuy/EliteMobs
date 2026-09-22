package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.api.QuestRewardEvent;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class CustomFetchObjective extends Objective {
    private static final long serialVersionUID = -3846169815392092956L;
    private static final Map<UUID, PendingRefresh> pendingRefreshes = new HashMap<>();

    @Getter
    private final String key;
    @Getter
    @Setter
    /**
     * This sets when a boss has been detected as having dropped the relevant item for the quest after the quest has been slated to begin.
     */
    private boolean readyToPickUp = false;
    /**
     * This sets whether the items are lost upon turning the quest in
     */
    @Getter
    private boolean requireItemTurnIn;

    public CustomFetchObjective(int targetAmount, String objectiveName, String customItemFilename) {
        super(targetAmount, objectiveName);
        this.key = customItemFilename;
    }

    private static void checkEvent(@NotNull Player player, @NotNull ItemStack itemStack) {
        if (!PlayerData.isInMemory(player)) return;
        for (Quest quest : PlayerData.getQuests(player.getUniqueId()))
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective)
                    ((CustomFetchObjective) objective).checkItem(player, itemStack, quest.getQuestObjectives());
    }

    private void checkItem(Player player, @NotNull ItemStack itemStack, QuestObjectives questObjectives) {
        if (!ItemTagger.hasKey(itemStack, this.key)) return;
        progressNonlinearObjective(questObjectives, player);
    }

    /** Validates all fetch requirements against one inventory snapshot before changing any slot. */
    public static boolean prepareTurnIn(QuestObjectives objectives, Player player, boolean consume) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Quest turn-in must run on the server thread");
        ItemStack[] planned = player.getInventory().getContents();
        for (int slot = 0; slot < planned.length; slot++)
            if (planned[slot] != null) planned[slot] = planned[slot].clone();
        boolean hasFetch = false;
        for (Objective objective : objectives.getObjectives()) {
            if (!(objective instanceof CustomFetchObjective fetch)) continue;
            hasFetch = true;
            fetch.fullUpdate(player);
            fetch.objectiveCompleted = fetch.currentAmount >= fetch.targetAmount;
            int remaining = fetch.targetAmount;
            for (int slot = 0; slot < planned.length && remaining > 0; slot++) {
                ItemStack item = planned[slot];
                if (!ItemTagger.hasKey(item, fetch.key)) continue;
                int taken = Math.min(remaining, item.getAmount());
                remaining -= taken;
                item.setAmount(item.getAmount() - taken);
                if (item.getAmount() == 0) planned[slot] = null;
            }
            if (remaining > 0) return false;
        }
        if (consume && hasFetch) player.getInventory().setContents(planned);
        return true;
    }

    /**
     * Updates a non-linear objective, sending an event
     *
     * @param questObjectives Objectives to update
     * @param player          Player to update
     */
    @Override
    public void progressNonlinearObjective(QuestObjectives questObjectives, Player player) {
        if (!PlayerData.isInMemory(player) || questObjectives.isTurnedIn()) return;
        PendingRefresh pending = pendingRefreshes.get(player.getUniqueId());
        if (pending == null) {
            pending = new PendingRefresh(player, PlayerData.getPlayerData(player.getUniqueId()));
            pendingRefreshes.put(player.getUniqueId(), pending);
            PendingRefresh scheduled = pending;
            pending.task = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> refresh(scheduled), 1L);
        }
        pending.objectives.computeIfAbsent(questObjectives, ignored -> new HashSet<>()).add(this);
    }

    public static void cancelRefresh(UUID playerId) {
        PendingRefresh pending = pendingRefreshes.remove(playerId);
        if (pending != null && pending.task != null) pending.task.cancel();
    }

    private static void refresh(PendingRefresh pending) {
        Player player = pending.player;
        if (!pendingRefreshes.remove(player.getUniqueId(), pending) || !player.isOnline()
                || Bukkit.getPlayer(player.getUniqueId()) != player
                || PlayerData.getPlayerData(player.getUniqueId()) != pending.owner) return;
        List<Quest> active = PlayerData.getQuests(player.getUniqueId());
        pending.objectives.keySet().removeIf(objectives -> objectives.isTurnedIn() || !active.contains(objectives.getQuest()));
        Map<NamespacedKey, Integer> counts = new HashMap<>();
        for (Set<CustomFetchObjective> objectives : pending.objectives.values())
            for (CustomFetchObjective objective : objectives) counts.put(new NamespacedKey(MetadataHandler.PLUGIN, objective.key), 0);
        if (counts.isEmpty()) return;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            var data = item.getItemMeta().getPersistentDataContainer();
            for (NamespacedKey key : data.getKeys())
                if (counts.containsKey(key) && data.has(key, PersistentDataType.STRING))
                    counts.computeIfPresent(key, (ignored, count) -> count + item.getAmount());
        }
        List<QuestProgressionEvent> changes = new ArrayList<>();
        pending.objectives.forEach((questObjectives, objectives) -> {
            for (CustomFetchObjective objective : objectives) {
                int amount = counts.get(new NamespacedKey(MetadataHandler.PLUGIN, objective.key));
                boolean completed = amount >= objective.targetAmount;
                if (objective.currentAmount == amount && objective.objectiveCompleted == completed) continue;
                objective.currentAmount = amount;
                objective.objectiveCompleted = completed;
                changes.add(new QuestProgressionEvent(player, questObjectives.getQuest(), objective));
            }
        });
        QuestProgressionEvent.fireBatch(player, changes);
    }

    private static final class PendingRefresh {
        private final Player player;
        private final PlayerData owner;
        private final Map<QuestObjectives, Set<CustomFetchObjective>> objectives = new LinkedHashMap<>();
        private BukkitTask task;

        private PendingRefresh(Player player, PlayerData owner) {
            this.player = player;
            this.owner = owner;
        }
    }

    /**
     * Fully updates the inventory and total amount
     *
     * @param player Player to update
     */
    private void fullUpdate(Player player) {
        super.currentAmount = 0;
        for (ItemStack itemStack : player.getInventory())
            if (ItemTagger.hasKey(itemStack, this.key))
                super.currentAmount += itemStack.getAmount();
    }

    public static class CustomFetchObjectiveEvents implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onItemDrop(PlayerDropItemEvent event) {
            checkEvent(event.getPlayer(), event.getItemDrop().getItemStack());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onItemPickup(EntityPickupItemEvent event) {
            if (event.getEntity().getType() != EntityType.PLAYER) return;
            checkEvent((Player) event.getEntity(), event.getItem().getItemStack());
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestAcceptEvent(QuestAcceptEvent event) {
            for (Objective objective : event.getQuest().getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective)
                    objective.progressNonlinearObjective(event.getQuest().getQuestObjectives(), event.getPlayer());
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestCompleteEvent(QuestCompleteEvent event) {
            if (!prepareTurnIn(event.getQuest().getQuestObjectives(), event.getPlayer(), false))
                event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestRewardEvent(QuestRewardEvent event) {
            for (Quest quest : PlayerData.getQuests(event.getPlayer().getUniqueId()))
                for (Objective objective : quest.getQuestObjectives().getObjectives())
                    if (objective instanceof CustomFetchObjective)
                        objective.progressNonlinearObjective(quest.getQuestObjectives(), event.getPlayer());
        }
    }


}
