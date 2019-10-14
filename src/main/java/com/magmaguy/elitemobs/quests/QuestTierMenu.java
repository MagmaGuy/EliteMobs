package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.menus.premade.QuestMenuConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class QuestTierMenu {

    private Inventory inventory;
    private int questTier;
    private List<EliteQuest> eliteQuests = new ArrayList<>();

    public QuestTierMenu(int questTier) {
        setQuestTier(questTier);
        setInventory();
    }

    private void setQuestTier(int questTier) {
        this.questTier = questTier;
    }

    public int getQuestTier() {
        return this.questTier;
    }

    private void setInventory() {
        //Can't do the entire title with the rank because the title is used to identify the menu type
        this.inventory = Bukkit.createInventory(null, 9, QuestMenuConfig.questSelectorMenuTitle
                + " " + ChatColorConverter.convert(GuildRank.getRankName(getQuestTier())));
        for (int i = 1; i < 4; i++) {
            EliteQuest eliteQuest = EliteQuest.generateRandomQuest(questTier);
            this.inventory.setItem(2 * i, eliteQuest.generateQuestItemStack());
            addKillAmountQuests(eliteQuest);
        }
    }

    private Inventory generatePlayerInventory(List<EliteQuest> localEliteQuests) {
        Inventory localInventory = Bukkit.createInventory(null, 9, QuestMenuConfig.questSelectorMenuTitle + " "
                + ChatColorConverter.convert(GuildRank.getRankName(getQuestTier())));
        for (int i = 1; i < 4; i++)
            localInventory.setItem(2 * i, localEliteQuests.get(i - 1).generateQuestItemStack());
        return localInventory;
    }

    public Inventory getInventory(Player player) {
        List<EliteQuest> eliteQuests = new ArrayList<>();
        boolean hasQuest = false;
        if (EliteQuest.hasPlayerQuest(player))
            for (EliteQuest eliteQuest : getEliteQuests()) {
                if (eliteQuest.getUuid().equals(EliteQuest.getPlayerQuest(player).getUuid())) {
                    eliteQuest = EliteQuest.getPlayerQuest(player);
                    eliteQuests.add(eliteQuest);
                    hasQuest = true;
                } else
                    eliteQuests.add(eliteQuest);
            }

        if (hasQuest)
            return generatePlayerInventory(eliteQuests);

        return this.inventory;
    }

    public void addKillAmountQuests(EliteQuest eliteQuest) {
        getEliteQuests().add(eliteQuest);
    }

    public List<EliteQuest> getEliteQuests() {
        return this.eliteQuests;
    }
}
