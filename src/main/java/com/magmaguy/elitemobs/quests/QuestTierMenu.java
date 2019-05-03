package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.utils.ObfuscatedStringHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class QuestTierMenu {

    private static final String TIER_MENU_KEY = ObfuscatedStringHandler.obfuscateString("//////");
    public static final String TIER_MENU_NAME = "EliteMobs Quest Selection" + TIER_MENU_KEY;

    private Inventory inventory;
    private int questTier;
    private List<PlayerQuest> playerQuests = new ArrayList<>();

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
        this.inventory = Bukkit.createInventory(null, 9, TIER_MENU_NAME + " " + ChatColorConverter.convert(GuildRank.getRankName(getQuestTier() + 10)));
        for (int i = 1; i < 4; i++) {
            PlayerQuest playerQuest = QuestRandomizer.generateQuest(questTier);
            this.inventory.setItem(2 * i, playerQuest.generateQuestItemStack());
            addKillAmountQuests(playerQuest);
        }
    }

    private Inventory generatePlayerInventory(List<PlayerQuest> localPlayerQuests) {
        Inventory localInventory = Bukkit.createInventory(null, 9, TIER_MENU_NAME + " " + ChatColorConverter.convert(GuildRank.getRankName(getQuestTier() + 10)));
        for (int i = 1; i < 4; i++)
            localInventory.setItem(2 * i, localPlayerQuests.get(i - 1).generateQuestItemStack());
        return localInventory;
    }

    public Inventory getInventory(Player player) {
        List<PlayerQuest> playerQuests = new ArrayList<>();
        boolean hasQuest = false;
        if (PlayerQuest.hasPlayerQuest(player))
            for (PlayerQuest playerQuest : getPlayerQuests()) {
                if (playerQuest.getQuestReward().equals(PlayerQuest.getPlayerQuest(player).getQuestReward())) {
                    playerQuest = PlayerQuest.getPlayerQuest(player);
                    playerQuests.add(playerQuest);
                    hasQuest = true;
                } else
                    playerQuests.add(playerQuest);
            }

        if (hasQuest)
            return generatePlayerInventory(playerQuests);

        return this.inventory;
    }

    public void addKillAmountQuests(PlayerQuest playerQuest) {
        getPlayerQuests().add(playerQuest);
    }

    public List<PlayerQuest> getPlayerQuests() {
        return this.playerQuests;
    }
}
