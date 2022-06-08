package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.api.QuestRewardEvent;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.WarningMessage;
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
import org.jetbrains.annotations.NotNull;

public class CustomFetchObjective extends Objective {

    @Getter
    private String key;
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

    private static void checkEvent(@NotNull Player player, @NotNull ItemStack itemStack, boolean increment) {
        for (Quest quest : PlayerData.getQuests(player.getUniqueId()))
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective)
                    ((CustomFetchObjective) objective).checkItem(player, itemStack, increment, quest.getQuestObjectives());
    }

    private void checkItem(Player player, @NotNull ItemStack itemStack, boolean increment, QuestObjectives questObjectives) {
        if (ItemTagger.hasKey(itemStack, this.key))
            if (increment)
                incrementCount(player, itemStack.getAmount(), questObjectives);
            else
                decrementCount(player, itemStack.getAmount(), questObjectives);
    }

    private void incrementCount(Player player, int amount, QuestObjectives questObjectives) {
        super.currentAmount += amount;
        checkProgress(player, questObjectives, amount);
    }

    private void decrementCount(Player player, int amount, QuestObjectives questObjectives) {
        super.currentAmount -= amount;
        checkProgress(player, questObjectives, -amount);
    }

    public void checkProgress(Player player, QuestObjectives questObjectives, int pendingAmount) {
        boolean strictCheck = super.currentAmount < 0 || super.currentAmount >= super.targetAmount;
        if (strictCheck) {
            if (pendingAmount > 0) strictCheck(player, pendingAmount);
            else strictCheck(player, 0);
        }
        progressNonlinearObjective(questObjectives, player, false);
    }

    private void strictCheck(Player player, int pendingAmount) {
        super.currentAmount = 0;
        for (ItemStack itemStack : player.getInventory())
            if (ItemTagger.hasKey(itemStack, this.key))
                super.currentAmount += itemStack.getAmount();
        super.currentAmount += pendingAmount;
    }

    private void turnItemsIn(Player player) {
        int deletedItems = 0;
        for (ItemStack itemStack : player.getInventory()) {
            if (ItemTagger.hasKey(itemStack, this.key)) {
                int existingAmount = itemStack.getAmount();
                int missingAmount = targetAmount - deletedItems;
                if (existingAmount >= missingAmount) {
                    existingAmount -= missingAmount;
                    deletedItems += missingAmount;
                } else
                    existingAmount = 0;
                itemStack.setAmount(existingAmount);
                if (deletedItems == targetAmount)
                    return;
            }
        }
        new WarningMessage("Player " + player.getName() + " managed to complete objective " + objectiveName + " without turning in the required amount of items! This isn't good, tell the developer!");
    }

    /**
     * Updates a non-linear objective, sending an event
     *
     * @param questObjectives Objectives to update
     * @param player          Player to update
     * @param fullUpdate      Whether the update should rescan the objectives
     */
    @Override
    public void progressNonlinearObjective(QuestObjectives questObjectives, Player player, boolean fullUpdate) {
        if (fullUpdate)
            fullUpdate(player);
        QuestProgressionEvent questProgressionEvent = new QuestProgressionEvent(
                Bukkit.getPlayer(questObjectives.getQuest().getPlayerUUID()),
                questObjectives.getQuest(),
                this);
        new EventCaller(questProgressionEvent);
        objectiveCompleted = currentAmount >= targetAmount;
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
            checkEvent(event.getPlayer(), event.getItemDrop().getItemStack(), false);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onItemPickup(EntityPickupItemEvent event) {
            if (event.getEntity().getType() != EntityType.PLAYER) return;
            checkEvent((Player) event.getEntity(), event.getItem().getItemStack(), true);
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestAcceptEvent(QuestAcceptEvent event) {
            for (Objective objective : event.getQuest().getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective)
                    ((CustomFetchObjective) objective).strictCheck(event.getPlayer(), 0);
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestCompleteEvent(QuestCompleteEvent event) {
            for (Objective objective : event.getQuest().getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective) {
                    ((CustomFetchObjective) objective).checkProgress(event.getPlayer(), event.getQuest().getQuestObjectives(), 0);
                    if (objective.getCurrentAmount() < objective.getTargetAmount())
                        event.setCancelled(true);
                }
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestRewardEvent(QuestRewardEvent event) {
            for (Objective objective : event.getQuest().getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective)
                    ((CustomFetchObjective) objective).turnItemsIn(event.getPlayer());
        }
    }


}
