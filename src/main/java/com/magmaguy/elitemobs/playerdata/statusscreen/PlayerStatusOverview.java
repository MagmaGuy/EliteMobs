package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

final class PlayerStatusOverview {
    private PlayerStatusOverview() {
    }

    static List<String> lines(Player player) {
        List<String> configuredLines = PlayerStatusMenuConfig.getLandingOverviewLines();
        if (configuredLines == null || configuredLines.isEmpty()) return List.of();

        String money = EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId()));
        String combatLevel = String.valueOf(CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
        List<?> quests = PlayerData.getQuests(player.getUniqueId());
        String activeQuests = String.valueOf(quests == null ? 0 : quests.size());
        String score = String.valueOf(PlayerData.getScore(player.getUniqueId()));

        List<String> parsedLines = new ArrayList<>(configuredLines.size());
        for (String configuredLine : configuredLines) {
            if (configuredLine == null) continue;
            parsedLines.add(resolve(configuredLine, money, combatLevel, activeQuests, score));
        }
        return parsedLines;
    }

    static String text(Player player) {
        return String.join("\n", lines(player));
    }

    static ItemStack decorateHeader(ItemStack configuredHeader, Player player) {
        if (configuredHeader == null) return null;

        ItemStack header = configuredHeader.clone();
        ItemMeta meta = header.getItemMeta();
        if (meta == null) return header;

        List<String> overviewLines = lines(player);
        if (meta.hasDisplayName()) meta.setDisplayName(resolveForPlayer(meta.getDisplayName(), player));

        List<String> lore = meta.hasLore() && meta.getLore() != null
                ? new ArrayList<>(meta.getLore())
                : new ArrayList<>();
        lore.replaceAll(line -> resolveForPlayer(line, player));
        if (!lore.isEmpty() && !overviewLines.isEmpty()) lore.add("");
        lore.addAll(overviewLines);
        meta.setLore(lore);
        header.setItemMeta(meta);
        return header;
    }

    private static String resolveForPlayer(String line, Player player) {
        String money = EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId()));
        String combatLevel = String.valueOf(CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
        List<?> quests = PlayerData.getQuests(player.getUniqueId());
        String activeQuests = String.valueOf(quests == null ? 0 : quests.size());
        String score = String.valueOf(PlayerData.getScore(player.getUniqueId()));
        return resolve(line, money, combatLevel, activeQuests, score);
    }

    private static String resolve(String line, String money, String combatLevel, String activeQuests, String score) {
        return line
                .replace("$money", money)
                .replace("$combatLevel", combatLevel)
                // Backwards-compatible alias for customized configs from before the Combat Level rename.
                .replace("$threat", combatLevel)
                .replace("$activeQuests", activeQuests)
                .replace("$score", score);
    }
}
