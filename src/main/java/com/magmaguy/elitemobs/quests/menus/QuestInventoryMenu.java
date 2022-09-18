package com.magmaguy.elitemobs.quests.menus;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class QuestInventoryMenu {
    private static final int trackEntry = 8;
    private static final int acceptEntry = 26;
    private static final HashMap<Inventory, QuestDirectory> questDirectories = new HashMap<>();
    private static final HashMap<Inventory, QuestInventory> questInventories = new HashMap<>();

    private QuestInventoryMenu() {
    }

    public static void generateInventoryQuestEntries(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
        if (quests.size() == 1)
            QuestInventoryMenu.generateInventoryQuestEntry(quests.get(0), player, npcEntity);
        else
            QuestInventoryMenu.generateInventoryQuestDirectory(quests, player, npcEntity);
    }

    public static void generateInventoryQuestDirectory(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
        String menuTitle = "Quests";
        Inventory questInventory = Bukkit.createInventory(player, 27, menuTitle);
        List<Integer> questSlots = new ArrayList<>(Arrays.asList(13, 11, 15, 9, 17, 10, 16, 12, 14, 8));
        Material acceptMaterial = Material.GREEN_STAINED_GLASS_PANE;
        Material inProgressMaterial = Material.RED_STAINED_GLASS_PANE;
        Material completeMaterial = Material.ORANGE_STAINED_GLASS_PANE;
        HashMap<Integer, Quest> questMap = new HashMap<>();
        for (int i = 0; i < quests.size() - 1; i++) {
            questMap.put(questSlots.get(i), quests.get(i));
            QuestMenu.QuestText questText = new QuestMenu.QuestText(quests.get(i), npcEntity, player);
            if (!quests.get(i).isAccepted())
                questInventory.setItem(questSlots.get(i), ItemStackGenerator.generateItemStack(acceptMaterial, questText.getHeader().getText()));
            else if (!quests.get(i).getQuestObjectives().isOver())
                questInventory.setItem(questSlots.get(i), ItemStackGenerator.generateItemStack(inProgressMaterial, questText.getHeader().getText()));
            else
                questInventory.setItem(questSlots.get(i), ItemStackGenerator.generateItemStack(completeMaterial, questText.getHeader().getText()));
        }

        new QuestDirectory(player, questMap, questInventory, npcEntity);
        player.openInventory(questInventory);
    }

    public static void generateInventoryQuestEntry(Quest quest, Player player, NPCEntity npcEntity) {
        QuestMenu.QuestText questText = new QuestMenu.QuestText(quest, npcEntity, player);
        String title = "";
        if (questText.getHeader().getText() != null)
            title = questText.getHeader().getText();
        Inventory questInventory = Bukkit.createInventory(player, 27, title);
        int titleEntry = 4;
        List<Integer> loreEntries = new ArrayList<>(Arrays.asList(13, 14, 12, 15, 11, 16, 10, 17, 9));
        List<Integer> objectivesEntries = new ArrayList<>(Arrays.asList(21, 20, 19, 18));
        List<Integer> rewardEntries = new ArrayList<>(Arrays.asList(23, 24, 25));

        Material titleMaterial = Material.PAINTING;
        Material trackingMaterial = Material.TARGET;
        Material loreMaterial = Material.BOOK;
        Material objectivesMaterial = Material.ITEM_FRAME;
        Material rewardsMaterial = Material.GOLD_INGOT;
        Material acceptMaterial = Material.EMERALD;

        questInventory.setItem(titleEntry, generateItemStackEntry(questText.getHeader(), new TextComponent(), titleMaterial).get(0));
        if (quest instanceof CustomQuest && quest.isAccepted())
            questInventory.setItem(trackEntry, generateItemStackEntry(questText.getTrack(), new TextComponent(), trackingMaterial).get(0));
        questInventory.setItem(acceptEntry, generateItemStackEntry(questText.getAccept(), new TextComponent(), acceptMaterial).get(0));
        if (quest instanceof CustomQuest)
            fillItemSlotLists(questInventory, loreEntries, new TextComponent(" "), questText.getBody(), loreMaterial);
        fillItemSlotLists(questInventory, objectivesEntries, questText.getFixedSummary(), questText.getSummary(), objectivesMaterial);
        fillItemSlotLists(questInventory, rewardEntries, questText.getFixedRewards(), questText.getRewards(), rewardsMaterial);

        new QuestInventory(player, quest, questInventory, npcEntity);
        player.openInventory(questInventory);
    }

    public static void fillItemSlotLists(Inventory inventory, List<Integer> entries, TextComponent title, List<TextComponent> textComponents, Material material) {
        List<ItemStack> loreItems = generateItemStackEntry(title, textComponents, material);
        List<Integer> exactEntriesAmount = new ArrayList<>(entries);
        exactEntriesAmount = exactEntriesAmount.subList(0, loreItems.size());
        Collections.sort(exactEntriesAmount);
        for (int i = 0; i < exactEntriesAmount.size(); i++)
            inventory.setItem(exactEntriesAmount.get(i), loreItems.get(i));
    }

    public static List<ItemStack> generateItemStackEntry(TextComponent title, List<TextComponent> textComponents, Material material) {
        List<String> list = new ArrayList<>();
        textComponents.forEach(component -> list.add(component.getText()));
        return generateParsedItemStackEntry(title.getText(), list, material);
    }

    public static List<ItemStack> generateItemStackEntry(TextComponent title, TextComponent textComponent, Material material) {
        return generateParsedItemStackEntry(title.getText(), Collections.singletonList(textComponent.getText()), material);
    }

    public static List<ItemStack> generateParsedItemStackEntry(String title, List<String> rawLore, Material material) {
        title = title.replace(ChatColor.BLACK.toString(), ChatColor.WHITE.toString());
        List<String> lore = new ArrayList<>();
        rawLore.forEach(raw -> lore.add(ChatColor.WHITE + raw.replace(ChatColor.BLACK.toString(), ChatColor.WHITE.toString())));
        int characterLimit = QuestsConfig.getItemEntryCharacterLimitBedrockMenu();
        List<ItemStack> itemStacks = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        lore.forEach(entry -> counter.addAndGet(entry.length()));
        if (counter.get() < characterLimit) {
            itemStacks.add(ItemStackGenerator.generateItemStack(material, title, lore));
            return itemStacks;
        }

        int maxCharactersPerLine = QuestsConfig.getHorizontalCharacterLimitBedrockMenu();
        List<String> currentList = new ArrayList<>();
        int currentCharacterCount = 0;
        for (String entry : lore) {
            entry = entry.replace(ChatColor.BLACK.toString(), ChatColor.WHITE.toString());
            if (!currentList.isEmpty() && entry.length() + currentCharacterCount > characterLimit) {
                itemStacks.add(ItemStackGenerator.generateItemStack(material, title, currentList));
                currentList.clear();
                currentCharacterCount = 0;
            }
            if (entry.length() > maxCharactersPerLine) {
                int size = 30;
                //for languages that have no spaces
                if (!entry.contains(" ")) {
                    List<String> substrings = new ArrayList<>((entry.length() + size - 1) / size);
                    for (int start = 0; start < entry.length(); start += size)
                        substrings.add(ChatColor.WHITE + entry.substring(start, Math.min(entry.length(), start + size)));
                    currentList.addAll(substrings);
                    //for other languages
                } else {
                    String[] splitBySpaces = entry.split(" ");
                    List<String> substrings = new ArrayList<>();
                    StringBuilder currentString = new StringBuilder();
                    currentString.append(ChatColor.WHITE);
                    for (String string : splitBySpaces) {
                        string += " ";
                        if (currentString.length() + string.length() > size) {
                            substrings.add(currentString.toString());
                            currentString = new StringBuilder();
                            currentString.append(ChatColor.WHITE);
                        }
                        currentString.append(string);
                    }
                    substrings.add(currentString.toString());
                    currentList.addAll(substrings);
                }
            } else
                currentList.add(entry);
            currentCharacterCount += entry.length();
        }
        itemStacks.add(ItemStackGenerator.generateItemStack(material, title, currentList));
        return itemStacks;
    }

    private static class QuestDirectory {
        HashMap<Integer, Quest> questMap;
        Inventory inventory;
        NPCEntity npcEntity;
        Player player;

        private QuestDirectory(Player player, HashMap<Integer, Quest> questMap, Inventory inventory, NPCEntity npcEntity) {
            this.questMap = questMap;
            this.inventory = inventory;
            this.npcEntity = npcEntity;
            this.player = player;
            questDirectories.put(inventory, this);
        }
    }

    private static class QuestInventory {
        Quest quest;
        Inventory inventory;
        NPCEntity npcEntity;
        Player player;

        private QuestInventory(Player player, Quest quest, Inventory inventory, NPCEntity npcEntity) {
            this.quest = quest;
            this.inventory = inventory;
            this.npcEntity = npcEntity;
            this.player = player;
            questInventories.put(inventory, this);
        }
    }

    public static class QuestInventoryMenuEvents implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (questDirectories.containsKey(event.getInventory())) {
                event.setCancelled(true);
                QuestDirectory questDirectory = questDirectories.get(event.getInventory());
                if (questDirectory.questMap.get(event.getSlot()) == null) return;
                player.closeInventory();
                generateInventoryQuestEntry(questDirectory.questMap.get(event.getSlot()), questDirectory.player, questDirectory.npcEntity);
            } else if (questInventories.containsKey(event.getInventory())) {
                event.setCancelled(true);
                switch (event.getSlot()) {
                    case trackEntry:
                        QuestCommand.trackQuest(questInventories.get(event.getInventory()).quest.getQuestID().toString(), player);
                        player.closeInventory();
                        break;
                    case acceptEntry:
                        Quest quest = questInventories.get(event.getInventory()).quest;
                        if (!quest.isAccepted())
                            QuestCommand.joinQuest(quest.getQuestID().toString(), player);
                        else if (!quest.getQuestObjectives().isOver())
                            QuestCommand.leaveQuest(player, quest.getQuestID().toString());
                        else
                            QuestCommand.completeQuest(quest.getQuestID().toString(), player);
                        player.closeInventory();
                        break;
                }
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInventoryClose(InventoryCloseEvent event) {
            questDirectories.remove(event.getInventory());
            questInventories.remove(event.getInventory());
        }

    }

}
