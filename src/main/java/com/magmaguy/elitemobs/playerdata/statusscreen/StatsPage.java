package com.magmaguy.elitemobs.playerdata.statusscreen;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StatsPage {
    private StatsPage() {
    }

    protected static TextComponent statsPage(Player targetPlayer) {
        TextComponent textComponent = new TextComponent();
        StatsSnapshot snapshot = StatsSnapshot.capture(targetPlayer);

        for (int i = 0; i < 13; i++) {
            if (PlayerStatusMenuConfig.getStatsTextLines()[i] == null) continue;
            if (PlayerStatusMenuConfig.getStatsTextLines()[i].contains("$guildtier")) continue;
            TextComponent line = new TextComponent(snapshot.replacePlaceholders(
                    PlayerStatusMenuConfig.getStatsTextLines()[i]) + "\n");

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
        StatsSnapshot snapshot = StatsSnapshot.capture(targetPlayer);
        inventory.setItem(PlayerStatusMenuConfig.getStatsRankSlot(),
                snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsRankItem()));
        inventory.setItem(PlayerStatusMenuConfig.getStatsScoreSlot(),
                snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsScoreItem()));
        inventory.setItem(PlayerStatusMenuConfig.getStatsDungeonsCompletedSlot(),
                snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsDungeonsCompletedItem()));
        inventory.setItem(PlayerStatusMenuConfig.getStatsCombatLevelSlot(),
                snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsCombatLevelItem()));
        inventory.setItem(PlayerStatusMenuConfig.getStatsEliteKillsSlot(),
                snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsEliteKillsItem()));
        inventory.setItem(PlayerStatusMenuConfig.getStatsMaxEliteLevelKilledSlot(),
                snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsMaxEliteLevelKilledItem()));
        inventory.setItem(PlayerStatusMenuConfig.getStatsQuestsCompletedSlot(),
                snapshot.replacePlaceholders(PlayerStatusMenuConfig.getStatsQuestsCompletedItem()));
        inventory.setItem(26, PlayerStatusMenuConfig.getBackItem());
        if (requestingPlayer.openInventory(inventory) == null) return;
        StatusInventorySafety.protect(inventory);
        StatsPageEvents.pageInventories.add(inventory);
    }

    /** Immutable values for one rendering of the Stats page. Reuse this in every UI surface. */
    public static final class StatsSnapshot {
        private final String money;
        private final String kills;
        private final String highestKill;
        private final String deaths;
        private final String questsCompleted;
        private final String dungeonsCompleted;
        private final String combatLevel;
        private final String score;
        private final String rank;
        private final String rankTotal;

        private StatsSnapshot(Player player) {
            UUID playerId = player.getUniqueId();
            PlayerData.ScoreRank scoreRank = PlayerData.getScoreRank(playerId);
            money = EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(playerId));
            kills = String.valueOf(PlayerData.getKills(playerId));
            highestKill = String.valueOf(PlayerData.getHighestLevelKilled(playerId));
            deaths = String.valueOf(PlayerData.getDeaths(playerId));
            questsCompleted = String.valueOf(PlayerData.getQuestsCompleted(playerId));
            dungeonsCompleted = String.valueOf(PlayerData.getDungeonsCompleted(playerId));
            combatLevel = String.valueOf(PlayerData.getPlayerLevel(playerId));
            score = String.valueOf(PlayerData.getScore(playerId));
            rank = scoreRank.isAvailable()
                    ? "#" + scoreRank.position()
                    : PlayerStatusMenuConfig.getStatsRankUnavailableText();
            rankTotal = scoreRank.isAvailable()
                    ? String.valueOf(scoreRank.playerCount())
                    : PlayerStatusMenuConfig.getStatsRankTotalUnavailableText();
        }

        public static StatsSnapshot capture(Player player) {
            return new StatsSnapshot(player);
        }

        public String replacePlaceholders(String value) {
            if (value == null) return "";
            return value
                    .replace("$ranktotal", rankTotal)
                    .replace("$rank", rank)
                    .replace("$dungeonsCompleted", dungeonsCompleted)
                    .replace("$dungeons", dungeonsCompleted)
                    .replace("$combatLevel", combatLevel)
                    .replace("$combat", combatLevel)
                    .replace("$questsCompleted", questsCompleted)
                    .replace("$highestkill", highestKill)
                    .replace("$maxKill", highestKill)
                    .replace("$money", money)
                    .replace("$kills", kills)
                    .replace("$deaths", deaths)
                    .replace("$quests", questsCompleted)
                    .replace("$score", score);
        }

        public ItemStack replacePlaceholders(ItemStack template) {
            ItemStack itemStack = template.clone();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) return itemStack;
            if (itemMeta.hasDisplayName())
                itemMeta.setDisplayName(replacePlaceholders(itemMeta.getDisplayName()));
            if (itemMeta.hasLore()) {
                List<String> lore = itemMeta.getLore();
                if (lore != null) {
                    List<String> replacedLore = new ArrayList<>(lore.size());
                    for (String line : lore) replacedLore.add(replacePlaceholders(line));
                    itemMeta.setLore(replacedLore);
                }
            }
            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }
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
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
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
