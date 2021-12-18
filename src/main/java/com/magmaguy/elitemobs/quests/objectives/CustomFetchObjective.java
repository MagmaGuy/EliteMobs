package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
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
        super.currentAmount++;
        checkProgress(player, questObjectives);
    }

    private void decrementCount(Player player, int amount, QuestObjectives questObjectives) {
        super.currentAmount--;
        checkProgress(player, questObjectives);
    }

    private void checkProgress(Player player, QuestObjectives questObjectives) {
        boolean strictCheck = super.currentAmount < 0 || super.currentAmount >= super.targetAmount;
        super.currentAmount = 0;
        if (strictCheck)
            for (ItemStack itemStack : player.getInventory())
                if (ItemTagger.hasKey(itemStack, this.key))
                    super.currentAmount += itemStack.getAmount();
        progressNonlinearObjective(questObjectives);
    }

    public static class CustomFetchObjectiveEvents implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onItemDrop(EntityDropItemEvent event) {
            if (event.getEntity().getType() != EntityType.PLAYER) return;
            checkEvent((Player) event.getEntity(), event.getItemDrop().getItemStack(), false);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onItemPickup(EntityPickupItemEvent event) {
            if (event.getEntity().getType() != EntityType.PLAYER) return;
            checkEvent((Player) event.getEntity(), event.getItem().getItemStack(), true);
        }

        @EventHandler(ignoreCancelled = true)
        public void onQuestCompleteEvent(QuestCompleteEvent event){
            for (Objective objective : event.getQuest().getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective){
                    ((CustomFetchObjective) objective).checkProgress(event.getPlayer(), event.getQuest().getQuestObjectives());
               if (objective.getCurrentAmount() < objective.getTargetAmount())
                   event.setCancelled(true);
                }
        }

    }


}
