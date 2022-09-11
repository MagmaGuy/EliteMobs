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

    private static void checkEvent(@NotNull Player player, @NotNull ItemStack itemStack, int newAmount) {
        for (Quest quest : PlayerData.getQuests(player.getUniqueId()))
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective)
                    ((CustomFetchObjective) objective).checkItem(player, itemStack, quest.getQuestObjectives(), newAmount);
    }

    private void checkItem(Player player, @NotNull ItemStack itemStack, QuestObjectives questObjectives, int newAmount) {
        if (ItemTagger.hasKey(itemStack, this.key))
            progressNonlinearObjective(questObjectives, player, newAmount);
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
     */
    @Override
    public void progressNonlinearObjective(QuestObjectives questObjectives, Player player, int newAmount) {
        fullUpdate(player, newAmount);
        objectiveCompleted = currentAmount >= targetAmount;
        QuestProgressionEvent questProgressionEvent = new QuestProgressionEvent(
                Bukkit.getPlayer(questObjectives.getQuest().getPlayerUUID()),
                questObjectives.getQuest(),
                this);
        new EventCaller(questProgressionEvent);
    }

    /**
     * Fully updates the inventory and total amount
     *
     * @param player Player to update
     */
    private void fullUpdate(Player player, int newAmount) {
        super.currentAmount = newAmount;
        for (ItemStack itemStack : player.getInventory())
            if (ItemTagger.hasKey(itemStack, this.key))
                super.currentAmount += itemStack.getAmount();
    }

    public static class CustomFetchObjectiveEvents implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onItemDrop(PlayerDropItemEvent event) {
            checkEvent(event.getPlayer(), event.getItemDrop().getItemStack(), 0);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onItemPickup(EntityPickupItemEvent event) {
            if (event.getEntity().getType() != EntityType.PLAYER) return;
            checkEvent((Player) event.getEntity(), event.getItem().getItemStack(), event.getItem().getItemStack().getAmount());
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestAcceptEvent(QuestAcceptEvent event) {
            for (Objective objective : event.getQuest().getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective)
                    ((CustomFetchObjective) objective).progressNonlinearObjective(event.getQuest().getQuestObjectives(), event.getPlayer(), 0);
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestCompleteEvent(QuestCompleteEvent event) {
            for (Objective objective : event.getQuest().getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective) {
                    ((CustomFetchObjective) objective).progressNonlinearObjective(event.getQuest().getQuestObjectives(), event.getPlayer(), 0);
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
