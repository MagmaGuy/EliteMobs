package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class CustomFetchObjective extends Objective{

    @Getter
    private String customItemFilename;
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
    @Getter
    /**
     * This sets whether the looting for the items must be done while the quest is active
     */
    private boolean requireLootingDuringQuest;


    public CustomFetchObjective(int targetAmount, String objectiveName, String customItemFilename){
        super(targetAmount, objectiveName);
        this.customItemFilename = customItemFilename;
    }


    private void checkProgress(Player player){
        for (ItemStack itemStack : player.getInventory())
            if (itemStack != null && itemStack.hasItemMeta()){
                //todo: yep this is still missing
            }
    }

    public static class CustomFetchObjectiveEvents implements Listener{
        @EventHandler
        public void onQuestAccept(QuestAcceptEvent questAcceptEvent){
            for (Objective objective : questAcceptEvent.getQuest().getQuestObjectives().getObjectives())
                if (objective instanceof CustomFetchObjective)
                    if (!((CustomFetchObjective) objective).requireLootingDuringQuest){
                        //todo: yep this is still missing
                    }

        }
    }

}
