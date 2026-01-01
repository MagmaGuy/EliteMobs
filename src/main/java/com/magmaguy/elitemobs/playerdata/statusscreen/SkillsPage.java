package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

/**
 * Skills page for the player status screen.
 * Displays all skill levels and XP progress.
 */
public class SkillsPage {

    private SkillsPage() {
    }

    /**
     * Creates the skills page for book-based UI.
     */
    protected static TextComponent skillsPage(Player targetPlayer) {
        TextComponent textComponent = new TextComponent();

        // Header
        TextComponent header = new TextComponent("\u00A75\u00A7lYour Skills\n\n");
        textComponent.addExtra(header);

        // Add each skill
        for (SkillType skillType : SkillType.values()) {
            long totalXP = PlayerData.getSkillXP(targetPlayer.getUniqueId(), skillType);
            int level = SkillXPCalculator.levelFromTotalXP(totalXP);
            long xpInLevel = SkillXPCalculator.xpProgressInCurrentLevel(totalXP);
            long xpForNext = SkillXPCalculator.xpToNextLevel(level);
            double progress = SkillXPCalculator.levelProgress(totalXP);

            // Create progress bar
            String progressBar = createProgressBar(progress);

            TextComponent line = new TextComponent(
                    "\u00A76" + skillType.getDisplayName() + " \u00A77Lv.\u00A7e" + level + "\n" +
                            "\u00A78" + progressBar + " \u00A77" + formatNumber(xpInLevel) + "/" + formatNumber(xpForNext) + "\n\n"
            );
            textComponent.addExtra(line);
        }

        return textComponent;
    }

    /**
     * Creates the skills page for inventory-based UI.
     */
    protected static void skillsPage(Player targetPlayer, Player requestingPlayer) {
        Inventory inventory = Bukkit.createInventory(requestingPlayer, 27, SkillsConfig.getSkillsMenuTitle().replace("&", "\u00A7"));

        // Skill slots: 10-16 (top row of items), could also use 2-8
        int[] skillSlots = {10, 11, 12, 13, 14, 15, 16};
        int slotIndex = 0;

        for (SkillType skillType : SkillType.values()) {
            if (slotIndex >= skillSlots.length) break;

            ItemStack skillItem = createSkillItem(targetPlayer, skillType);
            inventory.setItem(skillSlots[slotIndex], skillItem);
            slotIndex++;
        }

        // Back button
        inventory.setItem(26, PlayerStatusMenuConfig.getBackItem());

        requestingPlayer.openInventory(inventory);
        SkillsPageEvents.pageInventories.add(inventory);
    }

    /**
     * Creates an item representing a skill with its current level and XP.
     */
    private static ItemStack createSkillItem(Player player, SkillType skillType) {
        Material material = getMaterialForSkill(skillType);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        long totalXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
        int level = SkillXPCalculator.levelFromTotalXP(totalXP);
        long xpInLevel = SkillXPCalculator.xpProgressInCurrentLevel(totalXP);
        long xpForNext = SkillXPCalculator.xpToNextLevel(level);
        double progress = SkillXPCalculator.levelProgress(totalXP);

        // Display name with level
        meta.setDisplayName("\u00A76\u00A7l" + skillType.getDisplayName() + " \u00A77- Level \u00A7e" + level);

        // Lore with XP info
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("\u00A77XP Progress:");
        lore.add("\u00A78" + createProgressBar(progress));
        lore.add("\u00A77" + formatNumber(xpInLevel) + " / " + formatNumber(xpForNext) + " XP");
        lore.add("");
        lore.add("\u00A77Total XP: \u00A7e" + formatNumber(totalXP));

        // Kills to next level estimate (fighting same-level mobs)
        if (level < 200) {
            long killsNeeded = SkillXPCalculator.killsToNextLevel(level, level);
            lore.add("\u00A77Kills to next: \u00A7a~" + formatNumber(killsNeeded));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Gets an appropriate material to represent each skill.
     */
    private static Material getMaterialForSkill(SkillType skillType) {
        return switch (skillType) {
            case ARMOR -> Material.DIAMOND_CHESTPLATE;
            case SWORDS -> Material.DIAMOND_SWORD;
            case AXES -> Material.DIAMOND_AXE;
            case BOWS -> Material.BOW;
            case CROSSBOWS -> Material.CROSSBOW;
            case TRIDENTS -> Material.TRIDENT;
            case HOES -> Material.DIAMOND_HOE;
        };
    }

    /**
     * Creates a visual progress bar.
     */
    private static String createProgressBar(double progress) {
        int totalBars = 20;
        int filledBars = (int) (progress * totalBars);
        int emptyBars = totalBars - filledBars;

        StringBuilder bar = new StringBuilder("\u00A7a");
        for (int i = 0; i < filledBars; i++) {
            bar.append("|");
        }
        bar.append("\u00A77");
        for (int i = 0; i < emptyBars; i++) {
            bar.append("|");
        }

        return bar.toString();
    }

    /**
     * Formats large numbers with K, M suffixes.
     */
    private static String formatNumber(long number) {
        if (number >= 1_000_000_000) {
            return String.format("%.1fB", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        } else {
            return String.valueOf(number);
        }
    }

    public static class SkillsPageEvents implements Listener {
        private static final Set<Inventory> pageInventories = new HashSet<>();

        public static void shutdown() {
            pageInventories.clear();
        }

        @EventHandler
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.contains(event.getInventory())) return;
            event.setCancelled(true);

            // Back button
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
