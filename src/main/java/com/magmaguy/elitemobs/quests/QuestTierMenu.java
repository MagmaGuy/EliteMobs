package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.quests.dynamic.KillAmountQuest;
import com.magmaguy.elitemobs.utils.ObfuscatedStringHandler;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class QuestTierMenu {

    private static final String TIER_MENU_KEY = ObfuscatedStringHandler.obfuscateString("//////");
    public static final String TIER_MENU_NAME = "EliteMobs Quest Selection" + TIER_MENU_KEY;

    private Inventory inventory;
    private int questTier;
    private List<KillAmountQuest> killAmountQuests = new ArrayList<>();

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
            KillAmountQuest killAmountQuest = new KillAmountQuest(getQuestTier());
            this.inventory.setItem(2 * i, killAmountQuest.generateQuestItemStack());
            addKillAmountQuests(killAmountQuest);
        }
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void addKillAmountQuests(KillAmountQuest killAmountQuests) {
        getKillAmountQuests().add(killAmountQuests);
    }

    public List<KillAmountQuest> getKillAmountQuests() {
        return this.killAmountQuests;
    }
}
