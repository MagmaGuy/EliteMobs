package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.menus.premade.QuestMenuConfig;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import com.magmaguy.elitemobs.utils.MenuUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class QuestsMenu implements Listener {

    /**
     * Opens the main quest menu for a player. This contains all the ranks.
     *
     * @param player Player for whom the quest menu will open
     */
    public void initializeQuestTierSelectorMenu(Player player) {

        Inventory inventory = Bukkit.createInventory(player, 18, QuestMenuConfig.questTierSelectorMenuTitle);

        for (int index = 0; index < 12; index++) {

            if (GuildRank.isWithinActiveRank(player, index)) {

                HashMap<String, String> replacementItemStack = new HashMap<>();
                replacementItemStack.put("$rank", GuildRank.getRankName(index));
                inventory.setItem(index, ItemStackSerializer.itemStackPlaceholderReplacer(QuestMenuConfig.validTierButton, replacementItemStack));

            } else if (GuildRank.isWithinRank(player, index)) {

                HashMap<String, String> replacementItemStack = new HashMap<>();
                replacementItemStack.put("$rank", GuildRank.getRankName(index));
                inventory.setItem(index, ItemStackSerializer.itemStackPlaceholderReplacer(QuestMenuConfig.inactiveTierButton, replacementItemStack));

            } else {

                HashMap<String, String> replacementItemStack = new HashMap<>();
                replacementItemStack.put("$rank", GuildRank.getRankName(index));
                inventory.setItem(index, ItemStackSerializer.itemStackPlaceholderReplacer(QuestMenuConfig.invalidTierButton, replacementItemStack));

            }

        }

        player.openInventory(inventory);

    }

    @EventHandler
    public void onMainQuestClick(InventoryClickEvent event) {
        if (!MenuUtils.isValidMenu(event, QuestMenuConfig.questTierSelectorMenuTitle)) return;
        event.setCancelled(true);
        if (event.getInventory().getType().equals(InventoryType.PLAYER)) return;
        if (!event.getCurrentItem().getType().equals(Material.GREEN_STAINED_GLASS_PANE)) return;

        initializeTierQuestMenu((Player) event.getWhoClicked(), event.getSlot());
    }

    /**
     * Opens the tier quest menu for a player. This lists currently available quests.
     *
     * @param player   Player for whom the menu will open
     * @param menuSlot Menu slot, used to guess the tier of the quest
     */
    public void initializeTierQuestMenu(Player player, int menuSlot) {

        int questTier = menuSlot;

        player.openInventory(QuestRefresher.getQuestTierInventory(questTier).getInventory(player));

    }

    @EventHandler
    public void onTierQuestClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains(QuestMenuConfig.questSelectorMenuTitle)) return;
        if (!MenuUtils.isValidMenu(event)) return;
        event.setCancelled(true);
        if (event.getInventory().getType().equals(InventoryType.PLAYER)) return;

        int tier = 0;

        for (int i = 0; i < 11; i++)
            if (event.getView().getTitle().contains(GuildRank.getRankName(i))) {
                tier = i;
                break;
            }

        QuestTierMenu questTierMenu = QuestRefresher.getQuestTierInventory(tier);
        EliteQuest eliteQuest = questTierMenu.getEliteQuests().get(event.getSlot() / 2 - 1);

        if (EliteQuest.hasPlayerQuest((Player) event.getWhoClicked())) {
            initializeCancelQuestDialog((Player) event.getWhoClicked(), eliteQuest);
            event.getWhoClicked().closeInventory();
            return;
        }

        EliteQuest.addPlayerInQuests((Player) event.getWhoClicked(), eliteQuest);
        eliteQuest.getQuestObjective().sendQuestStartMessage((Player) event.getWhoClicked());
        event.getWhoClicked().closeInventory();

    }

    private static HashMap<Player, EliteQuest> questPairs = new HashMap();

    public static boolean playerHasPendingQuest(Player player) {
        return questPairs.containsKey(player);
    }

    public static EliteQuest getPlayerQuestPair(Player player) {
        return questPairs.get(player);
    }

    public static void removePlayerQuestPair(Player player) {
        questPairs.remove(player);
    }

    private void initializeCancelQuestDialog(Player player, EliteQuest eliteQuest) {
        player.sendMessage(QuestMenuConfig.cancelMessagePart1);
        TextComponent interactiveMessage = new TextComponent(QuestMenuConfig.cancelMessagePart2);
        interactiveMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs quest cancel " + player.getName() + " confirm"));
        interactiveMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Cancel!").create()));
        player.spigot().sendMessage(interactiveMessage);
        player.sendMessage(QuestMenuConfig.cancelMessagePart3);

        questPairs.put(player, eliteQuest);
    }

}
