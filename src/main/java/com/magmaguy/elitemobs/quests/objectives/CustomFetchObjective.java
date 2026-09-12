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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class CustomFetchObjective extends Objective {

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
        CustomFetchObjective customFetchObjective = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || questObjectives.isTurnedIn()) return;
                fullUpdate(player);
                objectiveCompleted = currentAmount >= targetAmount;
                QuestProgressionEvent questProgressionEvent = new QuestProgressionEvent(
                        Bukkit.getPlayer(questObjectives.getQuest().getPlayerUUID()),
                        questObjectives.getQuest(),
                        customFetchObjective);
                new EventCaller(questProgressionEvent);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
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
