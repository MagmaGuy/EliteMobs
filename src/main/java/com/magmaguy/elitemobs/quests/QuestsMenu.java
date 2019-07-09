package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.MenuUtils;
import com.magmaguy.elitemobs.utils.ObfuscatedStringHandler;
import net.md_5.bungee.api.ChatColor;
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

import java.util.Arrays;
import java.util.HashMap;

public class QuestsMenu implements Listener {

    private static final String MAIN_MENU_KEY = ObfuscatedStringHandler.obfuscateString("/////");
    private static final String MAIN_MENU_NAME = "EliteMobs Quests" + MAIN_MENU_KEY;

    /**
     * Opens the main quest menu for a player. This contains all the ranks.
     *
     * @param player Player for whom the quest menu will open
     */
    public void initializeMainQuestMenu(Player player) {

        Inventory inventory = Bukkit.createInventory(player, 18, MAIN_MENU_NAME);

        for (int index = 0; index < 11; index++) {

            if (GuildRank.isWithinActiveRank(player, index)) {
                inventory.setItem(index,
                        ItemStackGenerator.generateItemStack(Material.GREEN_STAINED_GLASS_PANE,
                                "&aAccept a " + GuildRank.getRankName(index + 1) + " &aquest!",
                                Arrays.asList("&aAccept a " + GuildRank.getRankName(index + 1) + " &aquest and", "&aget special rewards!")));

            } else if (GuildRank.isWithinRank(player, index + 1)) {
                inventory.setItem(index, ItemStackGenerator.generateItemStack(Material.YELLOW_STAINED_GLASS_PANE,
                        "&eYou can get a " + GuildRank.getRankName(index + 1) + " &equest!",
                        Arrays.asList("&eDo /ag and set your", "&eguild rank to " + GuildRank.getRankName(index), "&eto accept these quests!")));

            } else {
                inventory.setItem(index, ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS_PANE,
                        "&cYou can't get a " + GuildRank.getRankName(index + 1) + " &cquest yet!",
                        Arrays.asList("&cYou must first unlock the", GuildRank.getRankName(index + 1) + " &cguild rank at /ag", "&cto accept these quests!")));

            }

        }

        player.openInventory(inventory);

    }

    @EventHandler
    public void onMainQuestClick(InventoryClickEvent event) {
        if (!MenuUtils.isValidMenu(event, MAIN_MENU_NAME)) return;
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

        int questTier = menuSlot + 1;
        if (menuSlot == 13) questTier = 10;

        player.openInventory(QuestRefresher.getQuestTierInventory(questTier).getInventory(player));

    }

    @EventHandler
    public void onTierQuestClick(InventoryClickEvent event) throws CloneNotSupportedException {
        if (!event.getView().getTitle().contains(QuestTierMenu.TIER_MENU_NAME)) return;
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
        PlayerQuest playerQuest = questTierMenu.getPlayerQuests().get(event.getSlot() / 2 - 1);

        if (PlayerQuest.hasPlayerQuest((Player) event.getWhoClicked())) {
            initializeCancelQuestDialog((Player) event.getWhoClicked(), playerQuest);
            event.getWhoClicked().closeInventory();
            return;
        }

        PlayerQuest.addPlayerInQuests((Player) event.getWhoClicked(), playerQuest.clone());
        playerQuest.getQuestObjective().sendQuestStartMessage((Player) event.getWhoClicked());
        event.getWhoClicked().closeInventory();

    }

    private static HashMap<Player, PlayerQuest> questPairs = new HashMap();

    public static boolean playerHasPendingQuest(Player player) {
        return questPairs.containsKey(player);
    }

    public static PlayerQuest getPlayerQuestPair(Player player) {
        return questPairs.get(player);
    }

    public static void removePlayerQuestPair(Player player) {
        questPairs.remove(player);
    }

    private void initializeCancelQuestDialog(Player player, PlayerQuest playerQuest) {
        player.sendMessage(ChatColorConverter.convert("&c&l&m&o---------------------------------------------"));
        player.sendMessage(ChatColorConverter.convert("&c" + "You can only have one quest at a time! Cancelling your ongoing quest will reset quest progress!"));
        TextComponent interactiveMessage = new TextComponent("[Click here to cancel current quest!]");
        interactiveMessage.setColor(ChatColor.GREEN);
        interactiveMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs quest cancel " + player.getName() + " confirm"));
        interactiveMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Cancel!").create()));
        player.spigot().sendMessage(interactiveMessage);
        player.sendMessage(ChatColorConverter.convert("&7You can see your quest status with the command &a/em quest status"));
        player.sendMessage(ChatColorConverter.convert("&c&l&m&o---------------------------------------------"));

        questPairs.put(player, playerQuest);
    }

}
