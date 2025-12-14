package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;

public class StatsPage {
    private StatsPage() {
    }

    protected static TextComponent statsPage(Player targetPlayer) {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {
            if (PlayerStatusMenuConfig.getStatsTextLines()[i] == null) continue;
            TextComponent line = new TextComponent(PlayerStatusMenuConfig.getStatsTextLines()[i]
                    .replace("$money", EconomyHandler.checkCurrency(targetPlayer.getUniqueId()) + "")
                    .replace("$guildtier", PlayerStatusScreen.convertLightColorsToBlack(AdventurersGuildConfig.getShortenedRankName(GuildRank.getGuildPrestigeRank(targetPlayer), GuildRank.getActiveGuildRank(targetPlayer))))
                    .replace("$kills", PlayerData.getKills(targetPlayer.getUniqueId()) + "")
                    .replace("$highestkill", PlayerData.getHighestLevelKilled(targetPlayer.getUniqueId()) + "")
                    .replace("$deaths", PlayerData.getDeaths(targetPlayer.getUniqueId()) + "")
                    .replace("$quests", PlayerData.getQuestsCompleted(targetPlayer.getUniqueId()) + "")
                    .replace("$score", PlayerData.getScore(targetPlayer.getUniqueId()) + "") + "\n");

            if (PlayerStatusMenuConfig.getStatsHoverLines() != null &&
                    PlayerStatusMenuConfig.getStatsHoverLines()[i] != null
                    && !PlayerStatusMenuConfig.getStatsHoverLines()[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getStatsHoverLines()[i]);

            if (PlayerStatusMenuConfig.getStatsCommandLines() != null &&
                    PlayerStatusMenuConfig.getStatsCommandLines()[i] != null &&
                    !PlayerStatusMenuConfig.getStatsCommandLines()[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.getStatsCommandLines()[i]));

            textComponent.addExtra(line);
        }

        return textComponent;

    }

    protected static void statsPage(Player targetPlayer, Player requestingPlayer) {
        Inventory inventory = Bukkit.createInventory(requestingPlayer, 27, PlayerStatusMenuConfig.getStatsChestMenuName());
        inventory.setItem(PlayerStatusMenuConfig.getStatsMoneySlot(),
                replaceItemNamePlaceholder(PlayerStatusMenuConfig.getStatsMoneyItem().clone(), "$money",
                        EconomyHandler.checkCurrency(targetPlayer.getUniqueId()) + ""));
        inventory.setItem(PlayerStatusMenuConfig.getStatsGuildTierSlot(),
                replaceItemNamePlaceholder(PlayerStatusMenuConfig.getStatsGuildTierItem().clone(), "$tier",
                        AdventurersGuildConfig.getShortenedRankName(GuildRank.getGuildPrestigeRank(targetPlayer), GuildRank.getActiveGuildRank(targetPlayer))));
        inventory.setItem(PlayerStatusMenuConfig.getStatsEliteKillsSlot(),
                replaceItemNamePlaceholder(PlayerStatusMenuConfig.getStatsEliteKillsItem().clone(), "$kills",
                        PlayerData.getKills(targetPlayer.getUniqueId()) + ""));
        inventory.setItem(PlayerStatusMenuConfig.getStatsMaxEliteLevelKilledSlot(),
                replaceItemNamePlaceholder(PlayerStatusMenuConfig.getStatsMaxEliteLevelKilledItem().clone(), "$maxKill",
                        PlayerData.getHighestLevelKilled(targetPlayer.getUniqueId()) + ""));
        inventory.setItem(PlayerStatusMenuConfig.getStatsEliteDeathsSlot(),
                replaceItemNamePlaceholder(PlayerStatusMenuConfig.getStatsEliteDeathsItem().clone(), "$deaths",
                        PlayerData.getDeaths(targetPlayer.getUniqueId()) + ""));
        inventory.setItem(PlayerStatusMenuConfig.getStatsQuestsCompletedSlot(),
                replaceItemNamePlaceholder(PlayerStatusMenuConfig.getStatsQuestsCompletedItem().clone(), "$questsCompleted",
                        PlayerData.getQuestsCompleted(targetPlayer.getUniqueId()) + ""));
        inventory.setItem(PlayerStatusMenuConfig.getStatsScoreSlot(),
                replaceItemNamePlaceholder(PlayerStatusMenuConfig.getStatsScoreItem().clone(), "$score",
                        PlayerData.getScore(targetPlayer.getUniqueId()) + ""));
        inventory.setItem(26, PlayerStatusMenuConfig.getBackItem());
        requestingPlayer.openInventory(inventory);
        StatsPageEvents.pageInventories.add(inventory);
    }

    private static ItemStack replaceItemNamePlaceholder(ItemStack itemStack, String placeholder, String replacement) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(itemMeta.getDisplayName().replace(placeholder, replacement));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static class StatsPageEvents implements Listener {
        private static final Set<Inventory> pageInventories = new HashSet<>();

        public static void shutdown() {
            pageInventories.clear();
        }

        @EventHandler
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.contains(event.getInventory())) return;
            event.setCancelled(true);
            if (event.getSlot() == 26) {
                player.closeInventory();
                CoverPage.coverPage(player);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            pageInventories.remove(event.getInventory());
        }
    }
}
